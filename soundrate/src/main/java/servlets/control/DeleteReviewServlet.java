package servlets.control;

import application.business.DataAgent;
import application.exceptions.ReviewNotFoundException;
import application.model.Review;
import application.model.User;
import deezer.model.Album;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/delete-review"})
public class DeleteReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (reviewerUsername == null || reviewerUsername.isEmpty() ||
            reviewedAlbumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(reviewerUsername)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final User reviewer = this.dataAgent.getUser(reviewerUsername);
        if (reviewer == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Album reviewedAlbum = this.dataAgent.getAlbum(reviewedAlbumId);
        if (reviewedAlbum == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Review review = this.dataAgent.getReview(reviewerUsername, reviewedAlbumId);
        try {
            if (review == null)
                throw new ReviewNotFoundException();
            this.dataAgent.deleteReview(review);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ReviewNotFoundException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
