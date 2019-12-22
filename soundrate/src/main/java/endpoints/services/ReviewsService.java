package endpoints.services;

import application.entities.Report;
import application.entities.Review;
import application.entities.User;
import application.entities.Vote;
import application.model.CatalogAgent;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import application.model.exceptions.*;
import deezer.model.Album;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

@Path("/")
public class ReviewsService {

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private ReviewsAgent reviewsAgent;
    @Inject
    private CatalogAgent catalogAgent;
    @Inject
    private Validator validator;

    private Jsonb mapper;

    @PostConstruct
    private void init() {
        this.mapper = JsonbBuilder.create();
    }

    @Path("/get-review-vote")
    @GET
    public Response getReviewVote(@QueryParam("voter") @NotBlank final String voterUsername,
                                  @QueryParam("reviewer") @NotBlank final String reviewerUsername,
                                  @QueryParam("album") @NotNull final Long reviewedAlbumId,
                                  @Context HttpServletRequest request) {
        final User voter = this.usersAgent.getUser(voterUsername);
        if (voter == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.reviewNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Vote vote = this.reviewsAgent.getVote(voterUsername, reviewerUsername, reviewedAlbumId);
        return Response.ok(this.mapper.toJson(vote)).build();
    }

    @Path("/get-report")
    @GET
    public Response getReviewReport(@QueryParam("reporter") @NotBlank final String reporterUsername,
                                    @QueryParam("reviewer") @NotBlank final String reviewerUsername,
                                    @QueryParam("album") @NotNull final Long reviewedAlbumId,
                                    @Context HttpServletRequest request) {
        final User reporter = this.usersAgent.getUser(reporterUsername);
        if (reporter == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            final String responsePhrase = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.reviewNotFound");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final Report report = this.reviewsAgent.getReport(reporterUsername, reviewerUsername, reviewedAlbumId);
        return Response.ok(this.mapper.toJson(report)).build();
    }

    @Path("/get-reported-reviews")
    @GET
    public Response getReportedReviews(@QueryParam("index") @Min(0) final Integer index,
                                       @QueryParam("limit") @Min(1) final Integer limit,
                                       @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        final Boolean isModerator = sessionUser == null ?
                null :
                sessionUser.getRole() == User.Role.MODERATOR || sessionUser.getRole() == User.Role.ADMINISTRATOR;
        if (isModerator == null || !isModerator)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final List<Review> reportedReviews = this.reviewsAgent.getReportedReviews(index, limit);
        return Response.ok(this.mapper.toJson(reportedReviews)).build();
    }

    @Path("/publish-review")
    @POST
    public Response publishReview(@FormParam("reviewer") @NotBlank final String reviewerUsername,
                                  @FormParam("album") @NotNull final Long reviewedAlbumId,
                                  @FormParam("content") @NotBlank @Size(min = Review.MIN_CONTENT_LENGTH, max = Review.MAX_CONTENT_LENGTH)
                                      final String content,
                                  @FormParam("rating") @NotNull @Min(Review.MIN_ALLOWED_RATING) @Max(Review.MAX_ALLOWED_RATING)
                                      final Integer rating,
                                  @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(reviewerUsername))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User reviewer = this.usersAgent.getUser(reviewerUsername);
        if (reviewer == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Album reviewedAlbum = this.catalogAgent.getAlbum(reviewedAlbumId);
        if (reviewedAlbum == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.albumNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            review = new Review()
                    .setReviewer(reviewer)
                    .setReviewedAlbumId(reviewedAlbumId)
                    .setContent(content)
                    .setRating(rating)
                    .setPublicationDate(new Date());
            final Set<ConstraintViolation<Review>> constraintViolations = this.validator.validate(review);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            try {
                this.reviewsAgent.createReview(review);
            } catch (ConflictingReviewException e) {
                final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                        .getString("error.conflictingReview");
                return Response.status(Response.Status.CONFLICT).entity(response).build();
            }
        } else {
            review
                    .setContent(content)
                    .setRating(rating);
            final Set<ConstraintViolation<Review>> constraintViolations = this.validator.validate(review);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            try {
                this.reviewsAgent.updateReview(review);
            } catch (ReviewNotFoundException e) {
                final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                        .getString("error.reviewNotFound");
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }
        }
        return Response.ok().build();
    }

    @Path("/delete-review")
    @POST
    public Response deleteReview(@FormParam("reviewer") @NotBlank final String reviewerUsername,
                                 @FormParam("album") @NotNull final Long reviewedAlbumId,
                                 @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null
                || !(sessionUser.getUsername().equals(reviewerUsername)
                || sessionUser.getRole() == User.Role.MODERATOR
                || sessionUser.getRole() == User.Role.ADMINISTRATOR))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        try {
            if (review == null)
                throw new ReviewNotFoundException();
            this.reviewsAgent.deleteReview(review);
        } catch (ReviewNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.reviewNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        return Response.ok().build();
    }

    @Path("/vote-review")
    @POST
    public Response voteReview(@FormParam("voter") @NotBlank final String voterUsername,
                               @FormParam("reviewer") @NotBlank final String reviewerUsername,
                               @FormParam("album") @NotNull final Long reviewedAlbumId,
                               @FormParam("vote") final String voteValueParameter,
                               @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(voterUsername))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User voter = this.usersAgent.getUser(voterUsername);
        if (voter == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.reviewNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Boolean voteValue = voteValueParameter == null || voteValueParameter.isEmpty()
                ? null
                : Boolean.valueOf(voteValueParameter);
        Vote vote = this.reviewsAgent.getVote(voterUsername, reviewerUsername, reviewedAlbumId);
        try {
            if (vote == null) {
                if (voteValue == null)
                    throw new VoteNotFoundException();
                vote = new Vote()
                        .setVoter(voter)
                        .setReview(review)
                        .setValue(voteValue);
                final Set<ConstraintViolation<Vote>> constraintViolations = this.validator.validate(vote);
                if (!constraintViolations.isEmpty())
                    return Response.status(Response.Status.BAD_REQUEST).build();
                this.reviewsAgent.createVote(vote);
            } else {
                if (voteValue == vote.getValue())
                    throw new ConflictingVoteException();
                else if (voteValue == null)
                    this.reviewsAgent.deleteVote(vote);
                else {
                    vote.setValue(voteValue);
                    final Set<ConstraintViolation<Vote>> constraintViolations = this.validator.validate(vote);
                    if (!constraintViolations.isEmpty())
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    this.reviewsAgent.updateVote(vote);
                }
            }
        } catch (ConflictingVoteException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.conflictingVote");
            return Response.status(Response.Status.CONFLICT).build();
        } catch (VoteNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.voteNotFound");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @Path("/report-review")
    @POST
    public Response reportReview(@FormParam("reporter") @NotBlank final String reporterUsername,
                                 @FormParam("reviewer") @NotBlank final String reviewerUsername,
                                 @FormParam("album") @NotNull final Long reviewedAlbumId,
                                 @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User reporter = this.usersAgent.getUser(reporterUsername);
        if (reporter == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.reviewNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Report report = new Report()
                .setReporter(reporter)
                .setReview(review);
        final Set<ConstraintViolation<Review>> constraintViolations = this.validator.validate(review);
        if (!constraintViolations.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).build();
        try {
            this.reviewsAgent.createReport(report);
            return Response.ok().build();
        } catch (ConflictingReportException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.conflictingReport");
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }
    }

    @Path("/delete-review-reports")
    @POST
    public Response deleteReviewReports(@FormParam("reviewer") @NotBlank final String reviewerUsername,
                                        @FormParam("album") @NotNull final Long reviewedAlbumId,
                                        @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null
                || !(sessionUser.getRole() == User.Role.MODERATOR || sessionUser.getRole() == User.Role.ADMINISTRATOR))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        try {
            if (review == null)
                throw new ReviewNotFoundException();
            this.reviewsAgent.deleteReviewReports(review);
        } catch (ReviewNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.reviewNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        return Response.ok().build();
    }

}
