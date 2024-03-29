package endpoints.dispatchers.pages;

import application.entities.Review;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import deezer.model.Album;
import org.apache.commons.lang3.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/review"})
public class ReviewPageServlet extends HttpServlet {

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private ReviewsAgent reviewsAgent;
    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        final Boolean isModerator = sessionUser == null
                ? null
                : sessionUser.getRole() == User.Role.MODERATOR || sessionUser.getRole() == User.Role.ADMINISTRATOR;
        request.setAttribute("isModerator", isModerator);

        final Review review;
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (reviewerUsername == null || reviewerUsername.isEmpty()
                || reviewedAlbumId == Long.MIN_VALUE)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("review", review);

            final User reviewer = this.usersAgent.getUser(reviewerUsername);
            request.setAttribute("reviewer", reviewer);

            final Album reviewedAlbum = this.catalogAgent.getAlbum(reviewedAlbumId);
            request.setAttribute("reviewedAlbum", reviewedAlbum);

            final Integer reviewScore = this.reviewsAgent.getReviewScore(review);
            request.setAttribute("reviewScore", reviewScore);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/pages/review.jsp").forward(request, response);
    }

}
