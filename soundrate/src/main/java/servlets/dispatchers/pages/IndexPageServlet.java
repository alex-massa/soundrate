package servlets.dispatchers.pages;

import application.model.DataAgent;
import application.entities.Review;
import application.entities.User;
import deezer.model.Album;
import deezer.model.data.Albums;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/index", ""})
public class IndexPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int NUMBER_OF_ALBUMS = 8;
    private static final int NUMBER_OF_REVIEWS = 2;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Albums albums = this.dataAgent.getTopAlbums(0, NUMBER_OF_ALBUMS);
        request.setAttribute("albums", albums == null ? null : albums.getData());
        if (albums != null) {
            final Map<Album, Integer> albumNumberOfReviewsMap = albums.getData().stream().collect(
                    HashMap::new,
                    (map, album) -> map.put(album, this.dataAgent.getAlbumNumberOfReviews(album)),
                    HashMap::putAll
            );
            request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

            final Map<Album, Double> albumAverageRatingMap = albums.getData().stream().collect(
                    HashMap::new,
                    (map, album) -> map.put(album, this.dataAgent.getAlbumAverageRating(album)),
                    HashMap::putAll
            );
            request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
        }

        final List<Review> reviews = this.dataAgent.getTopReviews(0, IndexPageServlet.NUMBER_OF_REVIEWS);
        request.setAttribute("reviews", reviews);
        if (reviews != null) {
            final Map<Review, User> reviewerMap = reviews.stream().collect(
                    HashMap::new,
                    (map, review) -> map.put(review, this.dataAgent.getUser(review.getReviewerUsername())),
                    HashMap::putAll
            );
            request.setAttribute("reviewerMap", reviewerMap);

            final Map<Review, Album> reviewedAlbumMap = reviews.stream().collect(
                    HashMap::new,
                    (map, review) -> map.put(review, this.dataAgent.getAlbum(review.getReviewedAlbumId())),
                    HashMap::putAll
            );
            request.setAttribute("reviewedAlbumMap", reviewedAlbumMap);

            final Map<Review, Integer> reviewScoreMap = reviews.stream().collect(
                    HashMap::new,
                    (map, review) -> map.put(review, this.dataAgent.getReviewScore(review)),
                    HashMap::putAll
            );
            request.setAttribute("reviewScoreMap", reviewScoreMap);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/pages/index.jsp").forward(request, response);
    }

}
