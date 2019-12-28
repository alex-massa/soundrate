package application.model;

import application.entities.*;
import application.model.exceptions.*;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Singleton
@Lock(LockType.READ)
public class ReviewsAgent {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Review> getReviews() {
        return this.getReviews(null, null);
    }

    public List<Review> getReviews(@Min(0) final Integer index,
                                   @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        query.select(review);

        TypedQuery<Review> getReviewsQuery = this.entityManager.createQuery(query);
        if (index != null)
            getReviewsQuery.setFirstResult(index);
        if (limit != null)
            getReviewsQuery.setMaxResults(limit);
        return getReviewsQuery.getResultList();
    }

    public Review getReview(@NotNull final String reviewerUsername,
                            @NotNull final Long reviewedAlbumId) {
        Review.ReviewId reviewId = new Review.ReviewId()
                .setReviewerUsername(reviewerUsername)
                .setReviewedAlbumId(reviewedAlbumId);
        return this.entityManager.find(Review.class, reviewId);
    }

    public void createReview(@NotNull final Review review) {
        if (this.getReview(review.getReviewerUsername(), review.getReviewedAlbumId()) != null)
            throw new ConflictingReviewException();
        this.entityManager.persist(review);
    }

    public void updateReview(@NotNull final Review review) {
        if (this.getReview(review.getReviewerUsername(), review.getReviewedAlbumId()) == null)
            throw new ReviewNotFoundException();
        this.entityManager.merge(review);
    }

    public void deleteReview(@NotNull Review review) {
        if (this.getReview(review.getReviewerUsername(), review.getReviewedAlbumId()) == null)
            throw new ReviewNotFoundException();
        if (!this.entityManager.contains(review))
            review = this.entityManager.merge(review);
        this.entityManager.remove(review);
    }

    public List<Vote> getVotes() {
        return this.getVotes(null, null);
    }

    public List<Vote> getVotes(@Min(0) final Integer index,
                               @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        query.select(vote);

        TypedQuery<Vote> getVotesQuery = this.entityManager.createQuery(query);
        if (index != null)
            getVotesQuery.setFirstResult(index);
        if (limit != null)
            getVotesQuery.setMaxResults(limit);
        return getVotesQuery.getResultList();
    }

    public Vote getVote(@NotNull final String voterUsername,
                        @NotNull final String reviewerUsername,
                        @NotNull final Long reviewedAlbumId) {
        Review.ReviewId reviewId = new Review.ReviewId()
                .setReviewerUsername(reviewerUsername)
                .setReviewedAlbumId(reviewedAlbumId);
        Vote.VoteId voteId = new Vote.VoteId()
                .setVoterUsername(voterUsername)
                .setReviewId(reviewId);
        return this.entityManager.find(Vote.class, voteId);
    }

    public void createVote(@NotNull final Vote vote) {
        if (this.getVote(vote.getVoterUsername(), vote.getReviewerUsername(), vote.getReviewedAlbumId()) != null)
            throw new ConflictingVoteException();
        this.entityManager.persist(vote);
    }

    public void updateVote(@NotNull final Vote vote) {
        if (this.getVote(vote.getVoterUsername(), vote.getReviewerUsername(), vote.getReviewedAlbumId()) == null)
            throw new VoteNotFoundException();
        this.entityManager.merge(vote);
    }

    public void deleteVote(@NotNull Vote vote) {
        if (this.getVote(vote.getVoterUsername(), vote.getReviewerUsername(), vote.getReviewedAlbumId()) == null)
            throw new VoteNotFoundException();
        if (!this.entityManager.contains(vote))
            vote = this.entityManager.merge(vote);
        this.entityManager.remove(vote);
    }

    public List<Report> getReports() {
        return this.getReports(null, null);
    }

    public List<Report> getReports(@Min(0) final Integer index,
                                   @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Report> query = builder.createQuery(Report.class);
        Root<Report> report = query.from(Report.class);
        query.select(report);

        TypedQuery<Report> getReportsQuery = this.entityManager.createQuery(query);
        if (index != null)
            getReportsQuery.setFirstResult(index);
        if (limit != null)
            getReportsQuery.setMaxResults(limit);
        return getReportsQuery.getResultList();
    }

    public Report getReport(@NotNull final String reporterUsername,
                            @NotNull final String reviewerUsername,
                            @NotNull final Long reviewedAlbumId) {
        Review.ReviewId reviewId = new Review.ReviewId()
                .setReviewerUsername(reviewerUsername)
                .setReviewedAlbumId(reviewedAlbumId);
        Report.ReportId reportId = new Report.ReportId()
                .setReporterUsername(reporterUsername)
                .setReviewId(reviewId);
        return this.entityManager.find(Report.class, reportId);
    }

    public void createReport(@NotNull final Report report) {
        if (this.getReport(report.getReporterUsername(), report.getReviewerUsername(), report.getReviewedAlbumId()) != null)
            throw new ConflictingReportException();
        this.entityManager.persist(report);
    }

    public void updateReport(@NotNull final Report report) {
        if (this.getReport(report.getReporterUsername(), report.getReviewerUsername(), report.getReviewedAlbumId()) == null)
            throw new ReportNotFoundException();
        this.entityManager.merge(report);
    }

    public void deleteReport(@NotNull Report report) {
        if (this.getReport(report.getReporterUsername(), report.getReviewerUsername(), report.getReviewedAlbumId()) == null)
            throw new ReportNotFoundException();
        if (!this.entityManager.contains(report))
            report = this.entityManager.merge(report);
        this.entityManager.remove(report);
    }

    public List<Vote> getReviewVotes(@NotNull final Review review) {
        return this.getReviewVotes(review, null, null);
    }

    public List<Vote> getReviewVotes(@NotNull final Review review,
                                     @Min(0) final Integer index,
                                     @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        )
                ));

        TypedQuery<Vote> getReviewVotesQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        if (index != null)
            getReviewVotesQuery.setFirstResult(index);
        if (limit != null)
            getReviewVotesQuery.setMaxResults(limit);
        List<Vote> reviewVotes = getReviewVotesQuery.getResultList();
        return reviewVotes == null || reviewVotes.isEmpty() ? null : reviewVotes;
    }

    public @NotNull Integer getReviewNumberOfVotes(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.count(vote))
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        )
                ));

        TypedQuery<Long> getReviewNumberOfVotesQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewerUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        try {
            Long reviewNumberOfVotes = getReviewNumberOfVotesQuery.getSingleResult();
            return reviewNumberOfVotes == null ? 0 : Math.toIntExact(reviewNumberOfVotes);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteReviewVotes(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Vote> delete = builder.createCriteriaDelete(Vote.class);
        Root<Vote> vote = delete.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        delete
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter)
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId())
                .executeUpdate();
    }

    public List<Vote> getReviewUpvotes(@NotNull final Review review) {
        return this.getReviewUpvotes(review, null, null);
    }

    public List<Vote> getReviewUpvotes(@NotNull final Review review,
                                       @Min(0) final Integer index,
                                       @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                +1
                        )
                ));

        TypedQuery<Vote> getReviewUpvotesQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        if (index != null)
            getReviewUpvotesQuery.setFirstResult(index);
        if (limit != null)
            getReviewUpvotesQuery.setMaxResults(limit);
        List<Vote> reviewUpvotes = getReviewUpvotesQuery.getResultList();
        return reviewUpvotes == null || reviewUpvotes.isEmpty() ? null : reviewUpvotes;
    }

    public @NotNull Integer getReviewNumberOfUpvotes(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.count(vote))
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                +1
                        )
                ));

        TypedQuery<Long> getReviewNumberOfUpvotesQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewerUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        try {
            Long reviewNumberOfUpvotes = getReviewNumberOfUpvotesQuery.getSingleResult();
            return reviewNumberOfUpvotes == null ? 0 : Math.toIntExact(reviewNumberOfUpvotes);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteReviewUpvotes(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Vote> delete = builder.createCriteriaDelete(Vote.class);
        Root<Vote> vote = delete.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        delete
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter),
                        builder.equal(
                                vote.get(Vote_.value),
                                +1
                        )
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId())
                .executeUpdate();
    }

    public List<Vote> getReviewDownvotes(@NotNull final Review review) {
        return this.getReviewDownvotes(review, null, null);
    }

    public List<Vote> getReviewDownvotes(@NotNull final Review review,
                                         @Min(0) final Integer index,
                                         @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter),
                        builder.equal(
                                vote.get(Vote_.value),
                                -1
                        )
                ));

        TypedQuery<Vote> getReviewDownvotesQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        if (index != null)
            getReviewDownvotesQuery.setFirstResult(index);
        if (limit != null)
            getReviewDownvotesQuery.setMaxResults(limit);
        List<Vote> reviewDownvotes = getReviewDownvotesQuery.getResultList();
        return reviewDownvotes == null || reviewDownvotes.isEmpty() ? null : reviewDownvotes;
    }

    public @NotNull Integer getReviewNumberOfDownvotes(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.count(vote))
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                -1
                        )
                ));

        TypedQuery<Long> getReviewNumberOfDownvotesQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewerUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        try {
            Long reviewNumberOfDownvotes = getReviewNumberOfDownvotesQuery.getSingleResult();
            return reviewNumberOfDownvotes == null ? 0 : Math.toIntExact(reviewNumberOfDownvotes);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteReviewDownvotes(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Vote> delete = builder.createCriteriaDelete(Vote.class);
        Root<Vote> vote = delete.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        delete
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter),
                        builder.equal(
                                vote.get(Vote_.value),
                                -1
                        )
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId())
                .executeUpdate();
    }

    public List<Report> getReviewReports(@NotNull final Review review) {
        return this.getReviewReports(review, null, null);
    }

    public List<Report> getReviewReports(@NotNull final Review review,
                                         @Min(0) final Integer index,
                                         @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Report> query = builder.createQuery(Report.class);
        Root<Report> report = query.from(Report.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(report)
                .where(builder.and(
                        builder.equal(
                                report.get(Report_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                report.get(Report_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        )
                ));

        TypedQuery<Report> getReviewReportsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        if (index != null)
            getReviewReportsQuery.setFirstResult(index);
        if (limit != null)
            getReviewReportsQuery.setMaxResults(limit);
        List<Report> reviewReports = getReviewReportsQuery.getResultList();
        return reviewReports == null || reviewReports.isEmpty() ? null : reviewReports;
    }

    public @NotNull Integer getReviewNumberOfReports(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Report> report = query.from(Report.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.count(report))
                .where(builder.and(
                        builder.equal(
                                report.get(Report_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                report.get(Report_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        )
                ));

        TypedQuery<Long> getReviewNumberOfReportsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        try {
            Long reviewNumberOfReports = getReviewNumberOfReportsQuery.getSingleResult();
            return reviewNumberOfReports == null ? 0 : Math.toIntExact(reviewNumberOfReports);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteReviewReports(@NotNull final Review review) {
        /*  @fixme `Criteria API bulk deletion not yet supported by OpenJPA (current version 3.1.0 in TomEE 8.0.0)`
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Report> delete = builder.createCriteriaDelete(Report.class);
        Root<Report> report = delete.from(Report.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        delete
                .where(builder.and(
                        builder.equal(
                                report.get(Report_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                report.get(Report_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        )
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId())
                .executeUpdate();
        */

        this.entityManager.createQuery
                ("DELETE FROM Report r WHERE r.review.reviewer.username = :reviewerUsername AND r.review.reviewedAlbumId = :reviewedAlbumId")
                .setParameter("reviewerUsername", review.getReviewerUsername())
                .setParameter("reviewedAlbumId", review.getReviewedAlbumId())
                .executeUpdate();
    }

    public @NotNull Integer getReviewScore(@NotNull final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.sum(vote.get(Vote_.value)))
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                                reviewerUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.review).get(Review_.reviewedAlbumId),
                                reviewedAlbumIdParameter
                        )
                ));

        TypedQuery<Integer> getReviewScoreQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        try {
            Integer reviewScore = getReviewScoreQuery.getSingleResult();
            return reviewScore == null ? 0 : reviewScore;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public List<Review> getTopReviews() {
        return this.getTopReviews(null, null);
    }

    public List<Review> getTopReviews(@Min(0) final Integer index,
                                      @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        Join<Review, Vote> vote = review.join(Review_.votes, JoinType.LEFT);
        query
                .select(review)
                .groupBy(review)
                .orderBy(builder.desc(builder.coalesce(
                        builder.sum(vote.get(Vote_.value)),
                        builder.literal(0)
                )));

        TypedQuery<Review> getTopReviewsQuery = this.entityManager.createQuery(query);
        if (index != null)
            getTopReviewsQuery.setFirstResult(index);
        if (limit != null)
            getTopReviewsQuery.setMaxResults(limit);
        List<Review> topReviews = getTopReviewsQuery.getResultList();
        return topReviews == null || topReviews.isEmpty() ? null : topReviews;
    }

    public List<Review> getReportedReviews() {
        return this.getReportedReviews(null, null);
    }

    public List<Review> getReportedReviews(@Min(0) final Integer index,
                                           @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Report> report = query.from(Report.class);
        query
                .select(report.get(Report_.review))
                .groupBy(report.get(Report_.review))
                .orderBy(builder.desc(builder.count(report)));

        TypedQuery<Review> getReportedReviewsQuery = this.entityManager.createQuery(query);
        if (index != null)
            getReportedReviewsQuery.setFirstResult(index);
        if (limit != null)
            getReportedReviewsQuery.setMaxResults(limit);
        List<Review> reportedReviews = getReportedReviewsQuery.getResultList();
        return reportedReviews == null || reportedReviews.isEmpty() ? null : reportedReviews;
    }

}
