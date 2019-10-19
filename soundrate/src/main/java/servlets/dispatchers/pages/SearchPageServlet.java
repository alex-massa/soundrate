package servlets.dispatchers.pages;

import application.business.DataAgent;
import deezer.model.Album;
import deezer.model.Artist;

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

@WebServlet({"/search"})
public class SearchPageServlet extends HttpServlet {

    private static final long serialVersionUID = 7466750352140381400L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("q");
        if (query == null || query.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else {
            List<Artist> artists = this.dataAgent.searchArtists(query);
            request.setAttribute("artists", artists);
            if (artists != null) {
                Map<Artist, Integer> artistNumberOfReviewsMap = artists.stream().collect(
                        HashMap::new,
                        (map, artist) -> map.put(artist, this.dataAgent.getArtistNumberOfReviews(artist)),
                        HashMap::putAll
                );
                request.setAttribute("artistNumberOfReviewsMap", artistNumberOfReviewsMap);

                Map<Artist, Double> artistAverageRatingMap = artists.stream().collect(
                        HashMap::new,
                        (map, artist) -> map.put(artist, this.dataAgent.getArtistAverageRating(artist)),
                        HashMap::putAll
                );
                request.setAttribute("artistAverageRatingMap", artistAverageRatingMap);
            }

            List<Album> albums = this.dataAgent.searchAlbums(query);
            request.setAttribute("albums", albums);
            if (albums != null) {
                Map<Album, Integer> albumNumberOfReviewsMap = albums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumNumberOfReviews(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

                Map<Album, Double> albumAverageRatingMap = albums.stream().collect(
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
