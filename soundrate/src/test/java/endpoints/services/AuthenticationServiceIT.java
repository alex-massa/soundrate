package endpoints.services;

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
import java.util.Date;

@RunWith(Arquillian.class)
public class AuthenticationServiceIT {

    private static final String SESSION_COOKIE = "JSESSIONID";
    private static final String LOG_IN_ENDPOINT = "/log-in";
    private static final String LOG_OUT_ENDPOINT = "/log-out";
    private static final String SIGN_UP_ENDPOINT = "/sign-up";

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
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
        final UsersAgent usersAgent = CDI.current().select(UsersAgent.class).get();
        usersAgent.createUser(user);
    }

    /*
     *  Log-in tests
     */

    @Test
    @RunAsClient
    public void shouldFailLogInEmptyUsername() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(LOG_IN_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", null)
                .param("password", "password123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailLogInEmptyPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(LOG_IN_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "user")
                .param("password", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailLogInAlreadyAuthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(LOG_IN_ENDPOINT).request().cookie(sessionCookie).post(Entity.form(new Form()
                .param("username", "user")
                .param("password", "password123")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailLogInWrongCredentials() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        target.path(LOG_OUT_ENDPOINT).request().post(null);
        final Response response = target.path(LOG_IN_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "user")
                .param("password", "password")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassLogIn() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(LOG_IN_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "user")
                .param("password", "password123")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Log-out tests
     */

    @Test
    @RunAsClient
    public void shouldFailLogOutUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(LOG_OUT_ENDPOINT).request().post(null);
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassLogOut() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(LOG_OUT_ENDPOINT).request().cookie(sessionCookie).post(null);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Sign-up tests
     */

    @Test
    @RunAsClient
    public void shouldFaiSignUpEmptyUsername() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", null)
                .param("email", "otheruser@soundrate.com")
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailSignUpInvalidUsername() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "u")
                .param("password", "password123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFaiSignUpEmptyEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", null)
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFaiSignUpInvalidEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", "otheruser@.com")
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFaiSignUpEmptyPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", "otheruser@soundrate.com")
                .param("password", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFaiSignUpInvalidPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", "otheruser@soundrate.com")
                .param("password", "otherpassword")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFaiSignUpAlreadyAuthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(SIGN_UP_ENDPOINT).request().cookie(sessionCookie).post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", "otheruser@soundrate.com")
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailSignUpUnavailableUsername() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "user")
                .param("email", "otheruser@soundrate.com")
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailSignUpUnavailableEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", "user@soundrate.com")
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassSignUp() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(SIGN_UP_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "otheruser")
                .param("email", "otheruser@soundrate.com")
                .param("password", "otherpassword123")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
