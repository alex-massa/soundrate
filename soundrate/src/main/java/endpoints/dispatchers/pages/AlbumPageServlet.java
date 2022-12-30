package endpoints.dispatchers.pages;

import application.entities.Review;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import deezer.model.Album;
import deezer.model.Genre;
import org.apache.commons.lang3.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/album"})
public class AlbumPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsersAgent usersAgent;

    @Inject
    private ReviewsAgent reviewsAgent;
    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Album album;
        final long albumId = NumberUtils.toLong(request.getParameter("id"), Long.MIN_VALUE);
        if (albumId == Long.MIN_VALUE)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((album = this.catalogAgent.getAlbum(albumId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("album", album);

            final Genre albumGenre = album.getGenres() == null || album.getGenres().isEmpty()
                    ? null
                    : album.getGenres().getData().get(0);
            request.setAttribute("albumGenre", albumGenre);

            final List<Review> albumReviews = this.catalogAgent.getAlbumReviews(album);
            request.setAttribute("albumReviews", albumReviews);

            final int albumReviewsCount = this.catalogAgent.getAlbumReviewsCount(album);
            request.setAttribute("albumReviewsCount", albumReviewsCount);

            final Double albumAverageRating = this.catalogAgent.getAlbumAverageRating(album);
            request.setAttribute("albumAverageRating", albumAverageRating);

            if (albumReviews != null) {
                final Map<Review, User> reviewersMap = this.usersAgent.getReviewers(albumReviews);
                request.setAttribute("reviewersMap", reviewersMap);

                final Map<Review, Integer> reviewsScoresMap = this.reviewsAgent.getReviewsScores(albumReviews);
                request.setAttribute("reviewsScoresMap", reviewsScoresMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/album.jsp").forward(request, response);
    }

}
