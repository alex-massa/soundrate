package servlets.control;

import application.business.DataAgent;
import application.model.Review;
import application.model.User;
import deezer.model.Album;
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

@WebServlet({"/update-review"})
public class UpdateReviewServlet extends HttpServlet {

    private static final long serialVersionUID = -8450941344474125056L;

    @Inject
    private DataAgent dataAgent;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        if (albumId == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String action = (action = request.getParameter("action")) == null ? "" : action;
        switch (action) {
            case "publish": {
                String content = request.getParameter("content");
                Integer rating = NumberUtils.toInt(request.getParameter("rating"), -1);
                if (content == null || content.isEmpty() || rating < 1 || rating > 10) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                Album album = this.dataAgent.getAlbum(albumId);
                if (album == null) {
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                            request.getLocale()).getString("error.albumNotFound"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                Review review = this.dataAgent.getReview(sessionUsername, albumId);
                User reviewer = this.dataAgent.getUser(sessionUsername);
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
            }
            break;

            case "delete": {
                Review review = this.dataAgent.getReview(sessionUsername, albumId);
                if (review == null) {
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                            request.getLocale()).getString("error.reviewNotFound"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                this.dataAgent.deleteReview(review);
            }
            break;

            default: {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
