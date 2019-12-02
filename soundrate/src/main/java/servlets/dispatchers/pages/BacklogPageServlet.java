package servlets.dispatchers.pages;

import application.model.DataAgent;
import application.entities.BacklogEntry;
import application.entities.User;
import deezer.model.Album;
import deezer.model.Genre;

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
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/backlog"})
public class BacklogPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User user;
        final String username = request.getParameter("id");
        if (username == null || username.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((user = this.dataAgent.getUser(username)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("user", user);

            final List<BacklogEntry> userBacklog = this.dataAgent.getUserBacklog(user);

            if (userBacklog != null) {
                final List<Album> userBacklogAlbums = userBacklog.stream().map
                        (backlogEntry -> this.dataAgent.getAlbum(backlogEntry.getAlbumId()))
                        .collect(Collectors.toList());
                request.setAttribute("backlogAlbums", userBacklogAlbums);

                final Map<Album, Genre> albumGenreMap = userBacklogAlbums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumGenre(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumGenreMap", albumGenreMap);

                final Map<Album, Integer> albumNumberOfReviewsMap = userBacklogAlbums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumNumberOfReviews(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

                final Map<Album, Double> albumAverageRatingMap = userBacklogAlbums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumAverageRating(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/backlog.jsp").forward(request, response);
    }

}
