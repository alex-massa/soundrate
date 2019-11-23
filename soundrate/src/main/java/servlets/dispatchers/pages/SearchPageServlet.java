package servlets.dispatchers.pages;

import application.business.DataAgent;
import deezer.model.Album;
import deezer.model.Artist;
import deezer.model.data.Albums;
import deezer.model.data.Artists;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/search"})
public class SearchPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String query = request.getParameter("q");
        if (query == null || query.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else {
            final Artists artists = this.dataAgent.searchArtists(query);
            request.setAttribute("artists", artists == null ? null : artists.getData());
            if (artists != null) {
                final Map<Artist, Integer> artistNumberOfReviewsMap = artists.getData().stream().collect(
                        HashMap::new,
                        (map, artist) -> map.put(artist, this.dataAgent.getArtistNumberOfReviews(artist)),
                        HashMap::putAll
                );
                request.setAttribute("artistNumberOfReviewsMap", artistNumberOfReviewsMap);

                final Map<Artist, Double> artistAverageRatingMap = artists.getData().stream().collect(
                        HashMap::new,
                        (map, artist) -> map.put(artist, this.dataAgent.getArtistAverageRating(artist)),
                        HashMap::putAll
                );
                request.setAttribute("artistAverageRatingMap", artistAverageRatingMap);
            }

            final Albums albums = this.dataAgent.searchAlbums(query);
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
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/search.jsp").forward(request, response);
    }

}
