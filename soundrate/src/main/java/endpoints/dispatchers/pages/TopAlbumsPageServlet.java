package endpoints.dispatchers.pages;

import application.model.CatalogAgent;
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

@WebServlet(urlPatterns = {"/top"})
public class TopAlbumsPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int N_ALBUMS = 100;

    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final List<Album> topAlbums = this.catalogAgent.getTopAlbums(0, TopAlbumsPageServlet.N_ALBUMS);
        request.setAttribute("albums", topAlbums);
        if (topAlbums != null) {
            final Map<Album, Integer> albumsReviewsCountMap = this.catalogAgent.getAlbumsReviewsCount(topAlbums);
            request.setAttribute("albumsReviewsCountMap", albumsReviewsCountMap);

            final Map<Album, Double> albumsAverageRatingsMap = this.catalogAgent.getAlbumsAverageRatings(topAlbums);
            request.setAttribute("albumsAverageRatingsMap", albumsAverageRatingsMap);
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/top.jsp").forward(request, response);
    }

}
