package servlets.dispatchers.pages;

import application.model.DataAgent;
import application.entities.Review;
import application.entities.User;
import deezer.model.Album;

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

@WebServlet(urlPatterns = {"/user"})
public class UserPageServlet extends HttpServlet {

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

            final List<Review> userReviews = this.dataAgent.getUserReviews(user);
            request.setAttribute("userReviews", userReviews);

            final int userNumberOfReviews = this.dataAgent.getUserNumberOfReviews(user);
            request.setAttribute("userNumberOfReviews", userNumberOfReviews);

            final Double userAverageAssignedRating = this.dataAgent.getUserAverageAssignedRating(user);
            request.setAttribute("userAverageAssignedRating", userAverageAssignedRating);

            final int userReputation = this.dataAgent.getUserReputation(user);
            request.setAttribute("userReputation", userReputation);

            if (userReviews != null) {
                final Map<Review, Album> reviewedAlbumMap = userReviews.stream().collect(
                        HashMap::new,
                        (map, review) -> map.put(review, this.dataAgent.getAlbum(review.getReviewedAlbumId())),
                        HashMap::putAll
                );
                request.setAttribute("reviewedAlbumMap", reviewedAlbumMap);

                final Map<Review, Integer> reviewScoreMap = userReviews.stream().collect(
                        HashMap::new,
                        (map, review) -> map.put(review, this.dataAgent.getReviewScore(review)),
                        HashMap::putAll
                );
                request.setAttribute("reviewScoreMap", reviewScoreMap);
            }

            User sessionuser = (User) request.getSession().getAttribute("user");
            if (sessionuser != null
                    && sessionuser.getRole() == User.Role.ADMINISTRATOR
                    && !sessionuser.getUsername().equals(user.getUsername())) {
                final User.Role[] roles = User.Role.values();
                request.setAttribute("roles", roles);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/user.jsp").forward(request, response);
    }

}
