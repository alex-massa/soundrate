package endpoints.dispatchers.pages;

import application.model.CatalogAgent;
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
import java.util.Map;

@WebServlet(urlPatterns = {"/top"})
public class TopAlbumsPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int NUMBER_OF_ALBUMS = 100;

    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Albums topAlbums = this.catalogAgent.getTopAlbums(0, TopAlbumsPageServlet.NUMBER_OF_ALBUMS);
        request.setAttribute("albums", topAlbums == null ? null : topAlbums.getData());
        if (topAlbums != null) {
            final Map<Album, Integer> albumNumberOfReviewsMap = topAlbums.getData().stream().collect(
                    HashMap::new,
                    (map, album) -> map.put(album, this.catalogAgent.getAlbumNumberOfReviews(album)),
                    HashMap::putAll
            );
            request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

            final Map<Album, Double> albumAverageRatingMap = topAlbums.getData().stream().collect(
                    HashMap::new,
                    (map, album) -> map.put(album, this.catalogAgent.getAlbumAverageRating(album)),
                    HashMap::putAll
            );
            request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/top.jsp").forward(request, response);
    }

}
