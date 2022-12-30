package endpoints.dispatchers.pages;

import application.entities.Review;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import deezer.model.Album;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/index", ""})
public class IndexPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int N_ALBUMS = 8;
    private static final int N_REVIEWS = 2;

    @Inject
    private UsersAgent usersAgent;

    @Inject
    private ReviewsAgent reviewsAgent;
    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final List<Album> albums = this.catalogAgent.getTopAlbums(0, IndexPageServlet.N_ALBUMS);
        request.setAttribute("albums", albums);
        if (albums != null) {
            final Map<Album, Integer> albumReviewsCountMap = this.catalogAgent.getAlbumsReviewsCount(albums);
            request.setAttribute("albumReviewsCountMap", albumReviewsCountMap);

            final Map<Album, Double> albumAverageRatingMap = this.catalogAgent.getAlbumsAverageRatings(albums);
            request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
        }

        final List<Review> reviews = this.reviewsAgent.getTopReviews(0, IndexPageServlet.N_REVIEWS);
        request.setAttribute("reviews", reviews);
        if (reviews != null) {
            final Map<Review, User> reviewersMap = this.usersAgent.getReviewers(reviews);
            request.setAttribute("reviewersMap", reviewersMap);

            final Map<Review, Album> reviewedAlbumMap = this.catalogAgent.getReviewedAlbums(reviews);
            request.setAttribute("reviewedAlbumMap", reviewedAlbumMap);

            final Map<Review, Integer> reviewScoreMap = this.reviewsAgent.getReviewsScores(reviews);
            request.setAttribute("reviewScoreMap", reviewScoreMap);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/pages/index.jsp").forward(request, response);
    }

}
