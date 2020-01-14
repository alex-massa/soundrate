package endpoints.services;

import application.entities.Report;
import application.entities.Review;
import application.entities.User;
import application.entities.Vote;
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
public class ReviewsServiceIT {

    private static final String SESSION_COOKIE = "JSESSIONID";
    private static final String LOG_IN_ENDPOINT = "/log-in";
    private static final String PUBLISH_REVIEW_ENDPOINT = "/publish-review";
    private static final String UPDATE_REVIEW_ENDPOINT = "/publish-review";
    private static final String DELETE_REVIEW_ENDPOINT = "/delete-review";
    private static final String PUBLISH_REVIEW_VOTE_ENDPOINT = "/vote-review";
    private static final String UPDATE_REVIEW_VOTE_ENDPOINT = "/vote-review";
    private static final String DELETE_REVIEW_VOTE_ENDPOINT = "/vote-review";
    private static final String REPORT_REVIEW_ENDPOINT = "/report-review";
    private static final String DELETE_REVIEW_REPORTS_ENDPOINT = "/delete-review-reports";

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(ReviewsService.class)
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
        final User reviewer = new User()
                .setUsername("reviewer")
                .setEmail("reviewer@soundrate.com")
                .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setRole(User.Role.USER);
        final User voter = new User()
                .setUsername("voter")
                .setEmail("voter@soundrate.com")
                .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setRole(User.Role.USER);
        final User reporter = new User()
                .setUsername("reporter")
                .setEmail("reporter@soundrate.com")
                .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setRole(User.Role.USER);
        final User mod = new User()
                .setUsername("mod")
                .setEmail("mod@soundrate.com")
                .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setRole(User.Role.MODERATOR);
        final List<Review> reviews = Arrays.asList(
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(302127L)
                        .setContent("content")
                        .setRating(10)
                        .setPublicationDate(new Date()),
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(6575789L)
                        .setContent("content")
                        .setRating(1)
                        .setPublicationDate(new Date()),
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(1343199L)
                        .setContent("content")
                        .setRating(5)
                        .setPublicationDate(new Date()),
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(301728L)
                        .setContent("content")
                        .setRating(8)
                        .setPublicationDate(new Date()),
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(301775L)
                        .setContent("content")
                        .setRating(7)
                        .setPublicationDate(new Date()),
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(303459L)
                        .setContent("content")
                        .setRating(7)
                        .setPublicationDate(new Date()),
                new Review()
                        .setReviewer(reviewer)
                        .setReviewedAlbumId(299205L)
                        .setContent("content")
                        .setRating(6)
                        .setPublicationDate(new Date())
        );
        reviewer.setReviews(reviews);
        final List<Vote> votes = Arrays.asList(
                new Vote()
                        .setVoter(voter)
                        .setReview(reviews.get(0))
                        .setValue(true),
                new Vote()
                        .setVoter(voter)
                        .setReview(reviews.get(1))
                        .setValue(false),
                new Vote()
                        .setVoter(voter)
                        .setReview(reviews.get(2))
                        .setValue(true),
                new Vote()
                        .setVoter(voter)
                        .setReview(reviews.get(3))
                        .setValue(false)
        );
        voter.setVotes(votes);
        final List<Report> reports = Arrays.asList(
                new Report()
                        .setReporter(reporter)
                        .setReview(reviews.get(0)),
                new Report()
                        .setReporter(reporter)
                        .setReview(reviews.get(1))
        );
        reporter.setReports(reports);
        final UsersAgent usersAgent = CDI.current().select(UsersAgent.class).get();
        usersAgent.createUser(user);
        usersAgent.createUser(reviewer);
        usersAgent.createUser(voter);
        usersAgent.createUser(reporter);
        usersAgent.createUser(mod);
    }

    /*
     *  Publish review tests
     */

    @Test
    @RunAsClient
    public void shouldFailPublishReviewEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", null)
                .param("album", "302127")
                .param("content", "content")
                .param("rating", "10")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "user")
                .param("album", null)
                .param("content", "content")
                .param("rating", "10")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewEmptyContent() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "user")
                .param("album", "302127")
                .param("content", null)
                .param("rating", "10")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewEmptyRating() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "user")
                .param("album", "302127")
                .param("content", "content")
                .param("rating", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewRatingBelowMinAllowed() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "user")
                .param("album", "302127")
                .param("content", "content")
                .param("rating", "0")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewRatingAboveMaxAllowed() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "user")
                .param("album", "302127")
                .param("content", "content")
                .param("rating", "11")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "user")
                .param("album", "302127")
                .param("content", "content")
                .param("rating", "10")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "reviewer")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "user")
                        .param("album", "302127")
                        .param("content", "content")
                        .param("rating", "10")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewAlbumNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "user")
                        .param("album", "0")
                        .param("content", "content")
                        .param("rating", "10")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassPublishReview() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "user")
                        .param("album", "302127")
                        .param("content", "content")
                        .param("rating", "10")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Update review tests
     */

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", null)
                .param("album", "302127")
                .param("content", "updated content")
                .param("rating", "7")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", null)
                .param("content", "updated content")
                .param("rating", "7")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewEmptyContent() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("content", null)
                .param("rating", "7")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewEmptyRating() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("content", "updated content")
                .param("rating", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewRatingBelowMinAllowed() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("content", "updated content")
                .param("rating", "0")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewRatingAboveMaxAllowed() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("content", "updated content")
                .param("rating", "11")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("content", "updated content")
                .param("rating", "7")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "302127")
                        .param("content", "updated content")
                        .param("rating", "10")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassUpdateReview() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "reviewer")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "302127")
                        .param("content", "updated content")
                        .param("rating", "10")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Delete review tests
     */

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", null)
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewReviewNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "reviewer")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "301278")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassDeleteReview() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "reviewer")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "301775")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassDeleteReviewAsModerator() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "mod")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "303459")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Publish review vote tests
     */

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteEmptyVoter() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", null)
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("vote", "true")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "user")
                .param("reviewer", null)
                .param("album", "302127")
                .param("vote", "true")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "user")
                .param("reviewer", "reviewer")
                .param("album", null)
                .param("vote", "true")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "user")
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("vote", "true")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "voter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "user")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")
                        .param("vote", "true")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteReviewNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "user")
                        .param("reviewer", "reviewer")
                        .param("album", "301278")
                        .param("vote", "true")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailPublishReviewVoteConflictingVote() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "voter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "1343199")
                        .param("vote", "true")));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassPublishReviewVote() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(PUBLISH_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "user")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")
                        .param("vote", "true")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Update review vote tests
     */

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewVoteEmptyVoter() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", null)
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("vote", "false")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewVoteEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "voter")
                .param("reviewer", null)
                .param("album", "302127")
                .param("vote", "false")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewVoteEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "voter")
                .param("reviewer", "reviewer")
                .param("album", null)
                .param("vote", "false")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewVoteUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "voter")
                .param("reviewer", "reviewer")
                .param("album", "302127")
                .param("vote", "false")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewVoteAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")
                        .param("vote", "false")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailUpdateReviewVoteConflictingVote() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "voter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "6575789")
                        .param("vote", "false")));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassUpdateReviewVote() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "voter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(UPDATE_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")
                        .param("vote", "false")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Delete vote tests
     */

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewVoteEmptyVoter() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", null)
                .param("reviewer", "reviewer")
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewVoteEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "voter")
                .param("reviewer", null)
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewVoteEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "voter")
                .param("reviewer", "reviewer")
                .param("album", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewVoteUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request().post(Entity.form(new Form()
                .param("voter", "voter")
                .param("reviewer", "reviewer")
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewVoteAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewVoteAuthenticatedVoteNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "voter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "299205")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassDeleteReviewVote() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "voter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_VOTE_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("voter", "voter")
                        .param("reviewer", "reviewer")
                        .param("album", "301728")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Report review tests
     */

    @Test
    @RunAsClient
    public void shouldFailReportReviewEmptyReporter() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reporter", null)
                .param("reviewer", "reviewer")
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailReportReviewEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reporter", "user")
                .param("reviewer", null)
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailReportReviewEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reporter", "user")
                .param("reviewer", "reviewer")
                .param("album", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailReportReviewEmptyUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request().post(Entity.form(new Form()
                .param("reporter", "user")
                .param("reviewer", "reviewer")
                .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailReportReviewAuthenticatedToAnotherAccount() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "reporter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reporter", "user")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailReportReviewReviewNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reporter", "user")
                        .param("reviewer", "reviewer")
                        .param("album", "301278")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailReportReviewReviewConflictingReport() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "reporter")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reporter", "reporter")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassReportReviewReview() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(REPORT_REVIEW_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reporter", "user")
                        .param("reviewer", "reviewer")
                        .param("album", "302127")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /*
     *  Delete review reports tests
     */

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewReportsEmptyReviewer() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_REPORTS_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", null)
                .param("album", "6575789")));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewReportsEmptyAlbum() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_REPORTS_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", null)));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewReportsUnauthenticated() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Response response = target.path(DELETE_REVIEW_REPORTS_ENDPOINT).request().post(Entity.form(new Form()
                .param("reviewer", "reviewer")
                .param("album", "6575789")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewReportsUnauthorized() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "user")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_REPORTS_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "6575789")));
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldFailDeleteReviewReportsReviewNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "mod")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_REPORTS_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "301278")));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void shouldPassDeleteReviewReportsReviewNotFound() throws URISyntaxException {
        final WebTarget target = ClientBuilder.newClient().target(url.toURI());
        final Cookie sessionCookie = target.path(LOG_IN_ENDPOINT).request()
                .post(Entity.form(new Form()
                        .param("username", "mod")
                        .param("password", "password123")))
                .getCookies().get(SESSION_COOKIE);
        final Response response = target.path(DELETE_REVIEW_REPORTS_ENDPOINT).request()
                .cookie(sessionCookie)
                .post(Entity.form(new Form()
                        .param("reviewer", "reviewer")
                        .param("album", "6575789")));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
