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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(Arquillian.class)
public class UsersServiceIT {

    private static final String SESSION_COOKIE = "JSESSIONID";
    private static final String LOG_IN_ENDPOINT = "/log-in";
    private static final String UPDATE_USER_PASSWORD_ENDPOINT = "/update-user-password";
    private static final String UPDATE_USER_EMAIL_ENDPOINT = "/update-user-email";

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(UsersService.class)
                .addClass(AuthenticationService.class)
                .addPackages(true, "application.model", "application.entities")
                .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/resources.xml", "META-INF/resources.xml");
    }

    @BeforeClass
    public static void init() {
        final List<User> users = Arrays.asList(
                new User()
                        .setUsername("passworduser")
                        .setEmail("passworduser@soundrate.com")
                        .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setRole(User.Role.USER),
                new User()
                        .setUsername("passwordotheruser")
                        .setEmail("passwordotheruser@soundrate.com")
                        .setPassword(BCrypt.hashpw("otherpassword123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setRole(User.Role.USER),
                new User()
                        .setUsername("emailuser")
                        .setEmail("emailuser@soundrate.com")
                        .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setRole(User.Role.USER),
                new User()
                        .setUsername("emailotheruser")
                        .setEmail("emailotheruser@soundrate.com")
                        .setPassword(BCrypt.hashpw("otherpassword123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setRole(User.Role.USER),
                new User()
                        .setUsername("unavailableemailuser")
                        .setEmail("unavailable@soundrate.com")
                        .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setRole(User.Role.USER)
        );
        final UsersAgent usersAgent = CDI.current().select(UsersAgent.class).get();
        users.forEach(usersAgent::createUser);
    }

    /*
     *  Update user password tests
     */

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordEmptyUsername() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", null)
                .param("cpassword", "password123")
                .param("npassword", "newpassword123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordEmptyCurrentPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "passworduser")
                .param("cpassword", null)
                .param("npassword", "newpassword123")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordEmptyNewPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "passworduser")
                .param("cpassword", "password123")
                .param("npassword", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordInvalidNewPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "passworduser")
                .param("cpassword", "password123")
                .param("npassword", "newpassword")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "passworduser")
                .param("cpassword", "password123")
                .param("npassword", "newpassword123")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "passwordotheruser")
                        .param("password", "otherpassword123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "passworduser")
                        .param("cpassword", "otherpassword123")
                        .param("npassword", "newotherpassword123")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserPasswordWrongCurrentPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "passwordotheruser")
                        .param("password", "otherpassword123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "passwordotheruser")
                        .param("cpassword", "password123")
                        .param("npassword", "newotherpassword123")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassUpdateUserPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "passworduser")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_PASSWORD_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "passworduser")
                        .param("cpassword", "password123")
                        .param("npassword", "newpassword123")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Update user email tests
     */

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailEmptyUsername() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", null)
                .param("cpassword", "password123")
                .param("nemail", "emailuser@soundrate.co")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailEmptyCurrentPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "emailuser")
                .param("cpassword", null)
                .param("nemail", "emailuser@soundrate.co")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailEmptyNewEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "emailuser")
                .param("cpassword", "password123")
                .param("nemail", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailInvalidNewEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "emailuser")
                .param("cpassword", "password123")
                .param("nemail", "emailuser@.co")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request().post(Entity.form(new Form()
                .param("username", "emailuser")
                .param("cpassword", "password123")
                .param("nemail", "emailuser@soundrate.co")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "emailotheruser")
                        .param("password", "otherpassword123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "emailuser")
                        .param("cpassword", "otherpassword123")
                        .param("nemail", "emailotheruser@soundrate.co")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailWrongCurrentPassword() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "emailotheruser")
                        .param("password", "otherpassword123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "emailotheruser")
                        .param("cpassword", "password123")
                        .param("nemail", "emailotheruser@soundrate.co")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateUserEmailUnavailableEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "emailotheruser")
                        .param("password", "otherpassword123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "emailotheruser")
                        .param("cpassword", "otherpassword123")
                        .param("nemail", "unavailable@soundrate.com")));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassUpdateUserEmail() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "emailuser")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_USER_EMAIL_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("username", "emailuser")
                        .param("cpassword", "password123")
                        .param("nemail", "emailuser@soundrate.co")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
