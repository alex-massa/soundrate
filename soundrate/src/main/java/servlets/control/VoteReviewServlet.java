package servlets.control;

import application.entities.Review;
import application.entities.User;
import application.entities.Vote;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import application.model.exceptions.ConflictingVoteException;
import application.model.exceptions.VoteNotFoundException;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Set;

@WebServlet(urlPatterns = {"/vote-review"})
public class VoteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private ReviewsAgent reviewsAgent;

    @Inject
    private Validator validator;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String voterUsername = request.getParameter("voter");
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        final String voteValueParameter = request.getParameter("vote");
        if (voterUsername == null || voterUsername.isEmpty()
                || reviewerUsername == null || reviewerUsername.isEmpty()
                || reviewedAlbumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(voterUsername)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final User voter = this.usersAgent.getUser(voterUsername);
        if (voter == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
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
                if (!constraintViolations.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                this.reviewsAgent.createVote(vote);
            } else {
                if (voteValue == vote.getValue())
                    throw new ConflictingVoteException();
                else if (voteValue == null)
                    this.reviewsAgent.deleteVote(vote);
                else {
                    vote.setValue(voteValue);
                    final Set<ConstraintViolation<Vote>> constraintViolations = this.validator.validate(vote);
                    if (!constraintViolations.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                    this.reviewsAgent.updateVote(vote);
                }
            }
        } catch (ConflictingVoteException e) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.conflictingVote"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        } catch (VoteNotFoundException e) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.voteNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
