package servlets.dispatchers.pages;

import application.business.DataAgent;
import application.model.Review;
import application.model.User;
import deezer.model.Album;
import deezer.model.Genre;
import org.apache.commons.lang.math.NumberUtils;

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

@WebServlet({"/album"})
public class AlbumPageServlet extends HttpServlet {

    private static final long serialVersionUID = -8005646432043137746L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Album album;
        long albumId = NumberUtils.toLong(request.getParameter("id"), -1);
        if (albumId == -1)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((album = this.dataAgent.getAlbum(albumId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("album", album);

            Genre albumGenre = this.dataAgent.getAlbumGenre(album);
            request.setAttribute("albumGenre", albumGenre);

            List<Review> albumReviews = this.dataAgent.getAlbumReviews(album);
            request.setAttribute("albumReviews", albumReviews);

            int albumNumberOfReviews = this.dataAgent.getAlbumNumberOfReviews(album);
            request.setAttribute("albumNumberOfReviews", albumNumberOfReviews);

            Double albumAverageRating = this.dataAgent.getAlbumAverageRating(album);
            request.setAttribute("albumAverageRating", albumAverageRating);

            if (albumReviews != null) {
                Map<Review, Integer> reviewScoreMap = albumReviews.stream().collect(
                        HashMap::new,
                        (map, review) -> map.put(review, this.dataAgent.getReviewScore(review)),
                        HashMap::putAll
                );
                request.setAttribute("reviewScoreMap", reviewScoreMap);

                Map<Review, User> reviewerMap = albumReviews.stream().collect(
                        HashMap::new,
                        (map, review) -> map.put(review, this.dataAgent.getUser(review.getReviewerUsername())),
                        HashMap::putAll
                );
                request.setAttribute("reviewerMap", reviewerMap);
            }
        }

        request.getRequestDispatcher("/WEB-INF/jsp/pages/album.jsp").forward(request, response);
    }

}
