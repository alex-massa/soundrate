package servlets.data;

import application.entities.Review;
import application.entities.User;
import application.model.DataAgent;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/get-reported-reviews"})
public class GetReportedReviewsServlet extends HttpServlet {

    private Gson serializer;

    @Inject
    private DataAgent dataAgent;

    @Override
    public void init() {
        this.serializer = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        final Boolean isModerator = sessionUser == null ?
                null :
                sessionUser.getRole() == User.Role.MODERATOR || sessionUser.getRole() == User.Role.ADMINISTRATOR;
        if (isModerator == null || !isModerator) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final List<Review> reportedReviews = this.dataAgent.getReportedReviews();
        response.getWriter().write(this.serializer.toJson(reportedReviews));
    }

}
