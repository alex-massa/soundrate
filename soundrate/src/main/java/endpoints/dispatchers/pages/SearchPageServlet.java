package endpoints.dispatchers.pages;

import application.model.CatalogAgent;
import deezer.model.Album;
import deezer.model.Artist;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/search"})
public class SearchPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String query = request.getParameter("q");
        if (query == null || query.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else {
            final List<Artist> artists = this.catalogAgent.searchArtists(query);
            request.setAttribute("artists", artists);
            /*  @fixme API quota limit exceeded
            if (artists != null) {
                final Map<Artist, Integer> artistsReviewsCountMap =
                        this.catalogAgent.getArtistsReviewsCount(artists);
                request.setAttribute("artistsReviewsCountMap", artistsReviewsCountMap);

                final Map<Artist, Double> artistsAverageRatingsMap = this.catalogAgent.getArtistsAverageRatings(artists);
                request.setAttribute("artistAverageRatingMap", artistsAverageRatingsMap);
            }
            */

            final List<Album> albums = this.catalogAgent.searchAlbums(query);
            request.setAttribute("albums", albums);
            if (albums != null) {
                final Map<Album, Integer> albumsReviewsCountMap = this.catalogAgent.getAlbumsReviewsCount(albums);
                request.setAttribute("albumsReviewsCountMap", albumsReviewsCountMap);

                final Map<Album, Double> albumsAverageRatingsMap = this.catalogAgent.getAlbumsAverageRatings(albums);
                request.setAttribute("albumAverageRatingsMap", albumsAverageRatingsMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/search.jsp").forward(request, response);
    }

}
