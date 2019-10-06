package control.agents;

import model.access.LibraryAgent;
import model.access.UsersAgent;
import model.transfer.Album;
import model.transfer.Review;
import org.apache.commons.lang.math.NumberUtils;

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
                Album album = LibraryAgent.getAlbum(albumId);
                if (album == null) {
                    response.setContentType("text/plain");
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.albumNotFound"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                Review review = UsersAgent.getReview(albumId, sessionUsername);
                if (review == null) {
                    review = new Review()
                            .setReviewerUsername(sessionUsername)
                            .setReviewedAlbumId(albumId)
                            .setContent(content)
                            .setRating(rating)
                            .setPublicationDate(new Date());
                    UsersAgent.publishReview(review);
                } else
                    UsersAgent.editReview(review, content, rating);
            }
            break;

            case "delete": {
                Review review = UsersAgent.getReview(albumId, sessionUsername);
                if (review == null) {
                    response.setContentType("text/plain");
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.reviewNotFound"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                UsersAgent.deleteReview(review);
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
