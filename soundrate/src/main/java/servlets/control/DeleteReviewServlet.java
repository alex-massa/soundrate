package servlets.control;

import application.business.DataAgent;
import application.model.Review;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/delete-review"})
public class DeleteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionUsername;
        HttpSession session = request.getSession();
        synchronized (session) {
            sessionUsername = session.getAttribute("username") == null
                    ? null
                    : session.getAttribute("username").toString();
        }
        if (sessionUsername == null || sessionUsername.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long albumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (albumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Review review = this.dataAgent.getReview(sessionUsername, albumId);
        if (review == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        this.dataAgent.deleteReview(review);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
