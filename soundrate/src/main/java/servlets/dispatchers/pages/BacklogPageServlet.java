package servlets.dispatchers.pages;

import application.business.DataAgent;
import application.model.User;
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

@WebServlet({"/backlog"})
public class BacklogPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("id");
        if (username == null || username.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        User user = this.dataAgent.getUser(username);
        if (user == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("user", user);

            List<Album> userBacklog = this.dataAgent.getAlbumsInUserBacklog(user);
            request.setAttribute("backlog", userBacklog);

            if (userBacklog != null) {
                Map<Album, Genre> albumGenreMap = userBacklog.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumGenre(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumGenreMap", albumGenreMap);

                Map<Album, Integer> albumNumberOfReviewsMap = userBacklog.stream().collect(
                        HashMap::new,
                        (map, album) -> map.put(album, this.dataAgent.getAlbumNumberOfReviews(album)),
                        HashMap::putAll
                );
                request.setAttribute("albumNumberOfReviewsMap", albumNumberOfReviewsMap);

                Map<Album, Double> albumAverageRatingMap = userBacklog.stream().collect(
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
