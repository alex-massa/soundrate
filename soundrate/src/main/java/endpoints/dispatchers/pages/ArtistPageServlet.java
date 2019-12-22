package endpoints.dispatchers.pages;

import application.model.CatalogAgent;
import deezer.model.Album;
import deezer.model.Artist;
import deezer.model.data.Albums;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
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

            final Albums artistAlbums = this.catalogAgent.getArtistAlbums(artist);
            request.setAttribute("artistAlbums", artistAlbums == null ? null : artistAlbums.getData());

            final int artistNumberOfReviews = this.catalogAgent.getArtistNumberOfReviews(artist);
            request.setAttribute("artistNumberOfReviews", artistNumberOfReviews);

            final Double artistAverageRating = this.catalogAgent.getArtistAverageRating(artist);
            request.setAttribute("artistAverageRating", artistAverageRating);

            if (artistAlbums != null) {
                final Map<Album, Integer> albumNumberOfReviewsMap = artistAlbums.getData().stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.catalogAgent.getAlbumNumberOfReviews(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

                final Map<Album, Double> albumAverageRatingMap = artistAlbums.getData().stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.catalogAgent.getAlbumAverageRating(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/artist.jsp").forward(request, response);
    }

}
