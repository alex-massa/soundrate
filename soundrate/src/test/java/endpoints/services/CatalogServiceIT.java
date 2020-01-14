package endpoints.services;

import application.entities.BacklogEntry;
import application.entities.User;
import application.model.UsersAgent;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(Arquillian.class)
public class CatalogServiceIT {

    private static final String SESSION_COOKIE = "JSESSIONID";
    private static final String LOG_IN_ENDPOINT = "/log-in";
    private static final String CREATE_BACKLOG_ENTRY_ENDPOINT = "/update-backlog";
    private static final String DELETE_BACKLOG_ENTRY_ENDPOINT = "/update-backlog";

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(CatalogService.class)
                .addClass(AuthenticationService.class)
                .addPackages(true, "application.model", "application.entities")
                .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/resources.xml", "META-INF/resources.xml");
    }

    @BeforeClass
    public static void init() {
        final User user = new User()
                .setUsername("user")
                .setEmail("user@soundrate.com")
                .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setRole(User.Role.USER);
        final User otherUser = new User()
                .setUsername("otheruser")
                .setEmail("otheruser@soundrate.com")
                .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setRole(User.Role.USER);
        final List<BacklogEntry> userBacklog = Collections.singletonList(
                new BacklogEntry()
                        .setUser(user)
                        .setAlbumId(6575789L)
                        .setInsertionTime(new Date()));
        user.setBacklog(userBacklog);
        final UsersAgent usersAgent = CDI.current().select(UsersAgent.class).get();
        usersAgent.createUser(user);
        usersAgent.createUser(otherUser);
    }

    /*
     *  Create backlog entry tests
     */

    @Test
    @RunAsClient
    public void shouldFailCreateBacklogEntryEmptyUser() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(CREATE_BACKLOG_ENTRY_ENDPOINT).request().post(Entity.form(new Form()
                .param("user", null)
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailCreateBacklogEntryEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(CREATE_BACKLOG_ENTRY_ENDPOINT).request().post(Entity.form(new Form()
                .param("user", "user")
                .param("album", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailCreateBacklogEntryUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(CREATE_BACKLOG_ENTRY_ENDPOINT).request().post(Entity.form(new Form()
                .param("user", "user")
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailCreateBacklogEntryAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "otheruser")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(CREATE_BACKLOG_ENTRY_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("user", "user")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailCreateBacklogEntryAlbumNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "otheruser")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(CREATE_BACKLOG_ENTRY_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("user", "otheruser")
                        .param("album", "0")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassCreateBacklogEntry() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(CREATE_BACKLOG_ENTRY_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("user", "user")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Delete backlog entry tests
     */

    @Test
    @RunAsClient
    public void shouldFailDeleteBacklogEntryEmptyUser() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_BACKLOG_ENTRY_ENDPOINT).request().post(Entity.form(new Form()
                .param("user", null)
                .param("album", "6575789")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteBacklogEntryEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_BACKLOG_ENTRY_ENDPOINT).request().post(Entity.form(new Form()
                .param("user", "user")
                .param("album", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteBacklogEntryUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_BACKLOG_ENTRY_ENDPOINT).request().post(Entity.form(new Form()
                .param("user", "user")
                .param("album", "6575789")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteBacklogEntryAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "otheruser")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_BACKLOG_ENTRY_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("user", "user")
                        .param("album", "6575789")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassDeleteBacklogEntry() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_BACKLOG_ENTRY_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("user", "user")
                        .param("album", "6575789")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
