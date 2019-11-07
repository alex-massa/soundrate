package servlets.control;

import application.business.DataAgent;
import application.model.Review;
import application.model.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

@WebServlet({"/publish-review"})
public class PublishReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        String content = request.getParameter("content");
        content = StringEscapeUtils.escapeHtml(content);
        if (content == null || content.length() < Review.MIN_CONTENT_LENGTH || content.length() > Review.MAX_CONTENT_LENGTH) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int rating = NumberUtils.toInt(request.getParameter("rating"), Integer.MIN_VALUE);
        if (rating < Review.MIN_ALLOWED_RATING || rating > Review.MAX_ALLOWED_RATING) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        User reviewer = this.dataAgent.getUser(sessionUsername);
        if (reviewer == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Review review = this.dataAgent.getReview(sessionUsername, albumId);
        if (review == null) {
            review = new Review()
                    .setReviewer(reviewer)
                    .setReviewedAlbumId(albumId)
                    .setContent(content)
                    .setRating(rating)
                    .setPublicationDate(new Date());
            this.dataAgent.publishReview(review);
        } else
            this.dataAgent.editReview(review, content, rating);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
