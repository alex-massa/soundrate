package servlets.dispatchers.pages;

import application.business.DataAgent;
import deezer.model.Album;
import deezer.model.Artist;
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

@WebServlet({"/artist"})
public class ArtistPageServlet extends HttpServlet {

    private static final long serialVersionUID = -9200072939264770426L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Artist artist;
        long artistId = NumberUtils.toLong(request.getParameter("id"), -1);
        if (artistId == -1)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((artist = this.dataAgent.getArtist(artistId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("artist", artist);

            List<Album> artistAlbums = this.dataAgent.getArtistAlbums(artist);
            request.setAttribute("artistAlbums", artistAlbums);

            int artistNumberOfReviews = this.dataAgent.getArtistNumberOfReviews(artist);
            request.setAttribute("artistNumberOfReviews", artistNumberOfReviews);

            Double artistAverageRating = this.dataAgent.getArtistAverageRating(artist);
            request.setAttribute("artistAverageRating", artistAverageRating);

            if (artistAlbums != null) {
                Map<Album, Integer> albumNumberOfReviewsMap = artistAlbums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumNumberOfReviews(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

                Map<Album, Double> albumAverageRatingMap = artistAlbums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumAverageRating(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
            }
        }

        request.getRequestDispatcher("/WEB-INF/jsp/pages/artist.jsp").forward(request, response);
    }

}
