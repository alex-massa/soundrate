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

@WebServlet({"/vote-review"})
public class VoteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long albumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        String reviewerUsername = request.getParameter("reviewer");
        if (albumId == Long.MIN_VALUE || reviewerUsername == null || reviewerUsername.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User voter = this.dataAgent.getUser(sessionUser.getUsername());
        if (voter == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Review review = this.dataAgent.getReview(reviewerUsername, albumId);
        if (review == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String voteValueParameter = request.getParameter("vote");
        Boolean voteValue = voteValueParameter == null || voteValueParameter.isEmpty()
                ? null
                : Boolean.valueOf(voteValueParameter);
        Vote vote = this.dataAgent.getVote
                (voter.getUsername(), review.getReviewerUsername(), review.getReviewedAlbumId());
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
