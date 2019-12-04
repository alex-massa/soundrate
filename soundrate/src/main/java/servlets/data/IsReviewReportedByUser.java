package servlets.data;

import application.entities.Report;
import application.entities.Review;
import application.entities.User;
import application.model.DataAgent;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/is-review-reported-by-user"})
public class IsReviewReportedByUser extends HttpServlet {

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String reporterUsername = request.getParameter("reporter");
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (reporterUsername == null || reporterUsername.isEmpty()
                || reviewerUsername == null || reviewerUsername.isEmpty()
                || reviewedAlbumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User reporter = this.dataAgent.getUser(reporterUsername);
        if (reporter == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Review review = this.dataAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Report report = this.dataAgent.getReport(reporterUsername, reviewerUsername, reviewedAlbumId);
        response.getWriter().write(String.valueOf(report != null));
    }

}
