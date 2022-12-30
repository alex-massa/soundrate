package endpoints.dispatchers.pages;

import application.entities.BacklogEntry;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.UsersAgent;
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
    private UsersAgent usersAgent;
    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User user;
        final String username = request.getParameter("id");
        if (username == null || username.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((user = this.usersAgent.getUser(username)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("user", user);

            final List<BacklogEntry> userBacklog = this.usersAgent.getUserBacklog(user);

            if (userBacklog != null) {
                // @todo consider implementing as method in CatalogAgent
                final List<Album> userBacklogAlbums = userBacklog.stream().map
                        (backlogEntry -> this.catalogAgent.getAlbum(backlogEntry.getAlbumId()))
                        .collect(Collectors.toList());
                request.setAttribute("backlogAlbums", userBacklogAlbums);

                // @todo consider implementing as method in CatalogAgent
                final Map<Album, Genre> albumGenreMap = userBacklogAlbums.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, album.getGenres().isEmpty()
                                ? null
                                : album.getGenres().getData().get(0)),
                        HashMap::putAll
                );
                request.setAttribute("albumGenreMap", albumGenreMap);

                final Map<Album, Integer> albumReviewsCountMap =
                        this.catalogAgent.getAlbumsReviewsCount(userBacklogAlbums);
                request.setAttribute("albumReviewsCountMap", albumReviewsCountMap);

                final Map<Album, Double> albumAverageRatingMap =
                        this.catalogAgent.getAlbumsAverageRatings(userBacklogAlbums);
                request.setAttribute("albumAverageRatingMap", albumAverageRatingMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/backlog.jsp").forward(request, response);
    }

}
