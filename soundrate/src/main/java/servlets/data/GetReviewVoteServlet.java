package servlets.data;

import application.business.DataAgent;
import application.model.Review;
import application.model.User;
import application.model.Vote;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/get-review-vote-value"})
public class GetReviewVoteServlet extends HttpServlet {

    private static final long serialVersionUID = -5683567265876787274L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionUsername;
        synchronized (session) {
            sessionUsername = session.getAttribute("username") == null ? null : session.getAttribute("username").toString();
        }
        if (sessionUsername == null || sessionUsername.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long albumId = NumberUtils.toLong(request.getParameter("album"), -1);
        String reviewerUsername = request.getParameter("reviewer");
        if (albumId == -1 || reviewerUsername == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        User voter = this.dataAgent.getUser(sessionUsername);
        if (voter == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                    request.getLocale()).getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Review review = this.dataAgent.getReview(reviewerUsername, albumId);
        if (review == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                    request.getLocale()).getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Vote reviewUserVote = this.dataAgent.getUserReviewVote(voter, review);
        if (reviewUserVote == null)
            return;
        Boolean voteValue = null;
        if (reviewUserVote.getVote() == +1)
            voteValue = true;
        else if (reviewUserVote.getVote() == -1)
            voteValue = false;
        if (voteValue != null)
            response.getWriter().write(String.valueOf(voteValue));
    }

}
