package endpoints.dispatchers.pages;

import application.model.CatalogAgent;
import deezer.model.Album;
import deezer.model.Artist;
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

@WebServlet(urlPatterns = {"/artist"})
public class ArtistPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Artist artist;
        final long artistId = NumberUtils.toLong(request.getParameter("id"), Long.MIN_VALUE);
        if (artistId == Long.MIN_VALUE)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((artist = this.catalogAgent.getArtist(artistId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("artist", artist);

            final List<Album> artistAlbums = this.catalogAgent.getArtistAlbums(artist);
            request.setAttribute("artistAlbums", artistAlbums);

            final int artistReviewsCount = this.catalogAgent.getArtistReviewsCount(artist);
            request.setAttribute("artistReviewsCount", artistReviewsCount);

            final Double artistAverageRating = this.catalogAgent.getArtistAverageRating(artist);
            request.setAttribute("artistAverageRating", artistAverageRating);

            if (artistAlbums != null) {
                final Map<Album, Integer> albumsReviewsCountMap =
                        this.catalogAgent.getAlbumsReviewsCount(artistAlbums);
                request.setAttribute("albumReviewsCountMap", albumsReviewsCountMap);

                final Map<Album, Double> albumAverageRatingMap =
                        this.catalogAgent.getAlbumsAverageRatings(artistAlbums);
                request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/artist.jsp").forward(request, response);
    }

}
