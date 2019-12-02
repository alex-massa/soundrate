package application.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "report")
@IdClass(Report.ReportId.class)
public class Report implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporterUsername", referencedColumnName = "username")
    @NotNull(message = "{report.reporter.NotNull}")
    private User reporter;
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "reviewerUsername", referencedColumnName = "reviewerUsername"),
            @JoinColumn(name = "reviewedAlbumId", referencedColumnName = "reviewedAlbumId")
    })
    @NotNull(message = "{report.review.NotNull}")
    private Review review;

    public User getReporter() {
        return this.reporter;
    }

    public Report setReporter(User reporter) {
        this.reporter = reporter;
        return this;
    }

    public Review getReview() {
        return this.review;
    }

    public Report setReview(Review review) {
        this.review = review;
        return this;
    }

    public String getReporterUsername() {
        return this.reporter.getUsername();
    }

    public Report setReporterUsername(String reporterUsername) {
        this.reporter.setUsername(reporterUsername);
        return this;
    }

    public String getReviewerUsername() {
        return this.review.getReviewerUsername();
    }

    public Report setReviewerUsername(String reviewerUsername) {
        this.review.setReviewerUsername(reviewerUsername);
        return this;
    }

    public Long getReviewedAlbumId() {
        return this.review.getReviewedAlbumId();
    }

    public Report setReviewedAlbumId(Long reviewedAlbumId) {
        this.review.setReviewedAlbumId(reviewedAlbumId);
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Report.class.getSimpleName() + "{", "}")
                .add("reporterUsername=" + this.reporter.getUsername())
                .add("reviewerUsername=" + this.review.getReviewerUsername())
                .add("reviewedAlbumId=" + this.review.getReviewedAlbumId())
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;
        Report report = (Report) other;
        return  Objects.equals(this.reporter, report.reporter) &&
                Objects.equals(this.review, report.review);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reporter, this.review);
    }

    public static class ReportId implements Serializable {

        private static final long serialVersionUID = 1L;

        private String reporter;
        private Review.ReviewId review;

        public String getReporterUsername() {
            return this.reporter;
        }

        public ReportId setReporterUsername(String reporterUsername) {
            this.reporter = reporterUsername;
            return this;
        }

        public Review.ReviewId getReviewId() {
            return this.review;
        }

        public ReportId setReviewId(Review.ReviewId reviewId) {
            this.review = reviewId;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ReportId.class.getSimpleName() + "{", "}")
                    .add("reporterUsername=" + this.reporter)
                    .add("reviewerUsername=" + this.review.getReviewerUsername())
                    .add("reviewedAlbumId=" + this.review.getReviewedAlbumId())
                    .toString();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            if (object == null || this.getClass() != object.getClass())
                return false;
            ReportId reportId = (ReportId) object;
            return  Objects.equals(this.reporter, reportId.reporter) &&
                    Objects.equals(this.review, reportId.review);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.reporter, this.review);
        }

    }

}
