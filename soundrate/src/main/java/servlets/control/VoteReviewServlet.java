package servlets.control;

import application.business.DataAgent;
import application.exceptions.ConflictingVoteException;
import application.exceptions.VoteNotFoundException;
import application.model.Review;
import application.model.User;
import application.model.Vote;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/vote-review"})
public class VoteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String voterUsername = request.getParameter("voter");
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("reviewedAlbum"), Long.MIN_VALUE);
        final String voteValueParameter = request.getParameter("vote");
        if (voterUsername == null || voterUsername.isEmpty() ||
            reviewerUsername == null || reviewerUsername.isEmpty() ||
            reviewedAlbumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(voterUsername)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final User voter = this.dataAgent.getUser(voterUsername);
        if (voter == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Review review = this.dataAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Boolean voteValue = voteValueParameter == null || voteValueParameter.isEmpty()
                ? null
                : Boolean.valueOf(voteValueParameter);
        Vote vote = this.dataAgent.getVote(voterUsername, reviewerUsername, reviewedAlbumId);
        try {
            if (vote == null) {
                if (voteValue == null)
                    throw new VoteNotFoundException();
                vote = new Vote()
                        .setVoter(voter)
                        .setReview(review)
                        .setValue(voteValue);
                this.dataAgent.createVote(vote);
            } else {
                if (voteValue == vote.getValue())
                    throw new ConflictingVoteException();
                else if (voteValue == null)
                    this.dataAgent.deleteVote(vote);
                else {
                    vote.setValue(voteValue);
                    this.dataAgent.updateVote(vote);
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
