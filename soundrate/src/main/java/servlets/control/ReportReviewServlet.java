package servlets.control;

import application.entities.Report;
import application.entities.Review;
import application.entities.User;
import application.model.ReviewsAgent;
import application.model.UsersAgent;
import application.model.exceptions.ConflictingReportException;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Set;

@WebServlet(urlPatterns = {"/report-review"})
public class ReportReviewServlet extends HttpServlet {

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private ReviewsAgent reviewsAgent;

    @Inject
    private Validator validator;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String reporterUsername = request.getParameter("reporter");
        final String reviewerUsername = request.getParameter("reviewer");
        final long reviewedAlbumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (reporterUsername == null || reporterUsername.isEmpty()
                || reviewerUsername == null || reviewerUsername.isEmpty()
                || reviewedAlbumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(reporterUsername)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final User reporter = this.usersAgent.getUser(reporterUsername);
        if (reporter == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Review review = this.reviewsAgent.getReview(reviewerUsername, reviewedAlbumId);
        if (review == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings/strings",
                    request.getLocale()).getString("error.reviewNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Report report = new Report()
                .setReporter(reporter)
                .setReview(review);
        final Set<ConstraintViolation<Report>> constraintViolations = this.validator.validate(report);
        if (!constraintViolations.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            this.reviewsAgent.createReport(report);
        } catch (ConflictingReportException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.conflictingReport"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
