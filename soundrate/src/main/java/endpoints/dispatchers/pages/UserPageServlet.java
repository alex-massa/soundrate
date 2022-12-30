package endpoints.dispatchers.pages;

import application.entities.Review;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import deezer.model.Album;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/user"})
public class UserPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private ReviewsAgent reviewsAgent;
    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User sessionuser = (User) request.getSession().getAttribute("user");
        final Boolean isAdministrator = sessionuser == null
                ? null
                : sessionuser.getRole() == User.Role.ADMINISTRATOR;
        request.setAttribute("isAdministrator", isAdministrator);
        if (isAdministrator != null && isAdministrator)
            request.setAttribute("roles", User.Role.values());

        final User user;
        final String username = request.getParameter("id");
        if (username == null || username.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((user = this.usersAgent.getUser(username)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            request.setAttribute("user", user);

            final List<Review> userReviews = this.usersAgent.getUserReviews(user);
            request.setAttribute("userReviews", userReviews);

            final int userReviewsCount = this.usersAgent.getUserReviewsCount(user);
            request.setAttribute("userReviewsCount", userReviewsCount);

            final Double userAverageAssignedRating = this.usersAgent.getUserAverageAssignedRating(user);
            request.setAttribute("userAverageAssignedRating", userAverageAssignedRating);

            final int userReputation = this.usersAgent.getUserReputation(user);
            request.setAttribute("userReputation", userReputation);

            if (userReviews != null) {
                final Map<Review, Album> reviewedAlbumsMap = this.catalogAgent.getReviewedAlbums(userReviews);
                request.setAttribute("reviewedAlbumsMap", reviewedAlbumsMap);

                final Map<Review, Integer> reviewsScoresMap = this.reviewsAgent.getReviewsScores(userReviews);
                request.setAttribute("reviewsScoresMap", reviewsScoresMap);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/user.jsp").forward(request, response);
    }

}
