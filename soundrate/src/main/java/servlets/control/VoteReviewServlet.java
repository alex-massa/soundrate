package servlets.control;

import application.business.DataAgent;
import application.model.Review;
import application.model.User;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/vote-review"})
public class VoteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = -8970522315211670184L;

    @Inject
    private DataAgent usersAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionUsername;
        synchronized (session) {
            sessionUsername = session.getAttribute("username") == null ? null : session.getAttribute("username").toString();
        }
        if (sessionUsername == null || sessionUsername.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String voteValueParameter = request.getParameter("vote");
        Boolean voteValue =
                voteValueParameter == null || voteValueParameter.isEmpty()
                ? null
                : Boolean.valueOf(voteValueParameter);
        long albumId = NumberUtils.toLong(request.getParameter("album"), -1);
        String reviewerUsername = request.getParameter("reviewer");
        if (albumId == -1 || reviewerUsername == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        User voter = this.usersAgent.getUser(sessionUsername);
        if (voter == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                    request.getLocale()).getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Review review = this.usersAgent.getReview(reviewerUsername, albumId);
        if (review == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                    request.getLocale()).getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        this.usersAgent.voteReview(voter, review, voteValue);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
