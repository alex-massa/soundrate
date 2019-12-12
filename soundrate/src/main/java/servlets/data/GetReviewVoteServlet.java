package servlets.data;

import application.entities.Review;
import application.entities.User;
import application.entities.Vote;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/get-review-vote-value"})
public class GetReviewVoteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private ReviewsAgent reviewsAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String voterUsername = request.getParameter("voter");
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (voterUsername == null || voterUsername.isEmpty()
                || reviewerUsername == null || reviewerUsername.isEmpty()
                || reviewedAlbumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User voter = this.usersAgent.getUser(voterUsername);
        if (voter == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Vote reviewVote = this.reviewsAgent.getVote(voterUsername, reviewerUsername, reviewedAlbumId);
        if (reviewVote != null && reviewVote.getValue() != null)
            response.getWriter().write(String.valueOf(reviewVote.getValue()));
    }

}
