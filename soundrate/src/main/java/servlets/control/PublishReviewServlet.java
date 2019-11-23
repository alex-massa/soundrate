package servlets.control;

import application.business.DataAgent;
import application.exceptions.ConflictingReviewException;
import application.exceptions.ReviewNotFoundException;
import application.model.Review;
import application.model.User;
import deezer.model.Album;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/publish-review"})
public class PublishReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String reviewerUsername = request.getParameter("reviewer");
        final long albumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        final String content = StringEscapeUtils.escapeHtml(request.getParameter("content"));
        final int rating = NumberUtils.toInt(request.getParameter("rating"), Integer.MIN_VALUE);
        if (reviewerUsername == null || reviewerUsername.isEmpty() ||
            albumId == Long.MIN_VALUE ||
            content == null || content.length() < Review.MIN_CONTENT_LENGTH || content.length() > Review.MAX_CONTENT_LENGTH ||
            rating < Review.MIN_ALLOWED_RATING || rating > Review.MAX_ALLOWED_RATING) {
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final Album reviewedAlbum = this.dataAgent.getAlbum(albumId);
        if (reviewedAlbum == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Review review = this.dataAgent.getReview(reviewerUsername, albumId);
        if (review == null) {
            review = new Review()
                    .setReviewer(reviewer)
                    .setReviewedAlbumId(albumId)
                    .setContent(content)
                    .setRating(rating)
                    .setPublicationDate(new Date());
            try {
                this.dataAgent.createReview(review);
            } catch (ConflictingReviewException e) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.conflictingReview"));
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
        } else {
            review
                    .setContent(content)
                    .setRating(rating);
            try {
                this.dataAgent.updateReview(review);
            } catch (ReviewNotFoundException e) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.reviewNotFound"));
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
