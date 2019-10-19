package servlets.dispatchers.pages;

import application.business.DataAgent;
import deezer.model.Album;

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

@WebServlet({"/top"})
public class TopAlbumsPageServlet extends HttpServlet {

    private static final long serialVersionUID = -5787707038249348047L;

    private static final int NUMBER_OF_ALBUMS = 100;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Album> topAlbums = this.dataAgent.getTopAlbums(0, TopAlbumsPageServlet.NUMBER_OF_ALBUMS);
        request.setAttribute("albums", topAlbums);
        if (topAlbums != null) {
            Map<Album, Integer> albumNumberOfReviewsMap = topAlbums.stream().collect(
                    HashMap::new,
                    (map, album) -> map.put(album, this.dataAgent.getAlbumNumberOfReviews(album)),
                    HashMap::putAll
            );
            request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

            Map<Album, Double> albumAverageRatingMap = topAlbums.stream().collect(
                    HashMap::new,
                    (map, album) -> map.put(album, this.dataAgent.getAlbumAverageRating(album)),
                    HashMap::putAll
            );
            request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
        }

        request.getRequestDispatcher("/WEB-INF/jsp/pages/top.jsp").forward(request, response);
    }

}
