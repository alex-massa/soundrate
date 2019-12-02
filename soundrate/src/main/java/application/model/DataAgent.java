package application.model;

import application.model.exceptions.*;
import application.interceptors.bindings.Cacheable;
import application.interceptors.bindings.UserUpdate;
import application.entities.*;
import deezer.client.DeezerClient;
import deezer.client.DeezerClientException;
import deezer.model.Album;
import deezer.model.Artist;
import deezer.model.Genre;
import deezer.model.data.Albums;
import deezer.model.data.Artists;
import deezer.model.data.Genres;
import deezer.model.search.AlbumsSearch;
import deezer.model.search.ArtistsSearch;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.OptionalDouble;

@Stateless
public class DataAgent {

    @PersistenceContext(unitName = "soundratePersistenceUnit")
    private EntityManager entityManager;

    @Inject
    private DeezerClient client;

    public List<User> getUsers() {
        return this.getUsers(null, null);
    }

    public List<User> getUsers(@Min(0) Integer index,
                               @Min(1) Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> user = query.from(User.class);
        query.select(user);

        TypedQuery<User> getUsersQuery = this.entityManager.createQuery(query);
        if (index != null)
            getUsersQuery.setFirstResult(index);
        if (limit != null)
            getUsersQuery.setMaxResults(limit);
        return getUsersQuery.getResultList();
    }

    public User getUser(@NotNull final String username) {
        return this.entityManager.find(User.class, username);
    }

    public User getUserByEmail(@NotNull @Email final String email) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> user = query.from(User.class);
        ParameterExpression<String> emailParameter = builder.parameter(String.class);
        query
                .select(user)
                .where(builder.equal(
                        user.get(User_.email),
                        emailParameter
                ));

        TypedQuery<User> getUserByEmailQuery = this.entityManager.createQuery(query)
                .setParameter(emailParameter, email);
        try {
            return getUserByEmailQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void createUser(@NotNull final User user) {
        if (this.getUser(user.getUsername()) != null)
            throw new ConflictingUsernameException();
        if (this.getUserByEmail(user.getEmail()) != null)
            throw new ConflictingEmailAddressException();
        this.entityManager.persist(user);
    }

    @UserUpdate(type = "update")
    public void updateUser(@NotNull final User user) {
        if (this.getUser(user.getUsername()) == null)
            throw new UserNotFoundException();
        User other = this.getUserByEmail(user.getEmail());
        if (other != null && !user.getUsername().equals(other.getUsername()))
            throw new ConflictingEmailAddressException();
        this.entityManager.merge(user);
    }

    @UserUpdate(type = "delete")
    public void deleteUser(@NotNull User user) {
        if (this.getUser(user.getUsername()) == null)
            throw new UserNotFoundException();
        if (!this.entityManager.contains(user))
            user = this.entityManager.merge(user);
        this.entityManager.remove(user);
    }

    public List<BacklogEntry> getBacklogEntries() {
        return this.getBacklogEntries(null, null);
    }

    public List<BacklogEntry> getBacklogEntries(@Min(0) Integer index,
                                                @Min(1) Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<BacklogEntry> query = builder.createQuery(BacklogEntry.class);
        Root<BacklogEntry> backlogEntry = query.from(BacklogEntry.class);
        query.select(backlogEntry);

        TypedQuery<BacklogEntry> getBacklogEntriesQuery = this.entityManager.createQuery(query);
        if (index != null)
            getBacklogEntriesQuery.setFirstResult(index);
        if (limit != null)
            getBacklogEntriesQuery.setMaxResults(limit);
        return getBacklogEntriesQuery.getResultList();
    }

    public BacklogEntry getBacklogEntry(@NotNull final String username,
                                        @NotNull final Long albumId) {
        BacklogEntry.BacklogEntryId backlogEntryId = new BacklogEntry.BacklogEntryId()
                .setUsername(username)
                .setAlbumId(albumId);
        return this.entityManager.find(BacklogEntry.class, backlogEntryId);
    }

    public void createBacklogEntry(@NotNull BacklogEntry backlogEntry) {
        if (this.getBacklogEntry(backlogEntry.getUsername(), backlogEntry.getAlbumId()) != null)
            throw new ConflictingBacklogEntryException();
        this.entityManager.persist(backlogEntry);
    }

    public void updateBacklogEntry(@NotNull BacklogEntry backlogEntry) {
        if (this.getBacklogEntry(backlogEntry.getUsername(), backlogEntry.getAlbumId()) == null)
            throw new BacklogEntryNotFoundException();
        this.entityManager.merge(backlogEntry);
    }

    public void deleteBacklogEntry(@NotNull BacklogEntry backlogEntry) {
        if (this.getBacklogEntry(backlogEntry.getUsername(), backlogEntry.getAlbumId()) == null)
            throw new BacklogEntryNotFoundException();
        if (!this.entityManager.contains(backlogEntry))
            backlogEntry = this.entityManager.merge(backlogEntry);
        this.entityManager.remove(backlogEntry);
    }

    public List<Review> getReviews() {
        return this.getReviews(null, null);
    }

    public List<Review> getReviews(@Min(0) Integer index,
                                   @Min(1) Integer limit) {
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

    public List<Vote> getVotes(@Min(0) Integer index,
                               @Min(1) Integer limit) {
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

    public List<Report> getReports(@Min(0) Integer index,
                                   @Min(1) Integer limit) {
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

    public List<Review> getUserReviews(@NotNull final User user) {
        return this.getUserReviews(user, null, null);
    }

    public List<Review> getUserReviews(@NotNull final User user,
                                       @Min(0) final Integer index,
                                       @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        query
                .select(review)
                .where(builder.equal(
                        review.get(Review_.reviewer).get(User_.username),
                        reviewerUsernameParameter
                ))
                .orderBy(builder.desc(review.get(Review_.publicationDate)));

        TypedQuery<Review> getUserReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, user.getUsername());
        if (index != null)
            getUserReviewsQuery.setFirstResult(index);
        if (limit != null)
            getUserReviewsQuery.setMaxResults(limit);
        List<Review> userReviews = getUserReviewsQuery.getResultList();
        return userReviews == null || userReviews.isEmpty() ? null : userReviews;
    }

    public @NotNull Integer getUserNumberOfReviews(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(review))
                .where(builder.equal(
                        review.get(Review_.reviewer).get(User_.username),
                        reviewerUsernameParameter
                ));

        TypedQuery<Long> getUserNumberOfReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, user.getUsername());
        try {
            Long userNumberOfReviews = getUserNumberOfReviewsQuery.getSingleResult();
            return userNumberOfReviews == null ? 0 : Math.toIntExact(userNumberOfReviews);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteUserReviews(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Review> delete = builder.createCriteriaDelete(Review.class);
        Root<Review> review = delete.from(Review.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        delete
                .where(builder.equal(
                        review.get(Review_.reviewer).get(User_.username),
                        reviewerUsernameParameter
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reviewerUsernameParameter, user.getUsername())
                .executeUpdate();
    }

    public List<Vote> getUserVotes(@NotNull final User user) {
        return this.getUserVotes(user, null, null);
    }

    public List<Vote> getUserVotes(@NotNull final User user,
                                   @Min(0) final Integer index,
                                   @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        query
                .select(vote)
                .where(builder.equal(
                        vote.get(Vote_.voter).get(User_.username),
                        voterUsernameParameter
                ));

        TypedQuery<Vote> getUserVotesQuery = this.entityManager.createQuery(query)
                .setParameter(voterUsernameParameter, user.getUsername());
        List<Vote> userVotes = getUserVotesQuery.getResultList();
        return userVotes == null || userVotes.isEmpty() ? null : userVotes;
    }

    public @NotNull Integer getUserNumberOfVotes(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(vote))
                .where(builder.equal(
                        vote.get(Vote_.voter).get(User_.username),
                        voterUsernameParameter
                ));

        TypedQuery<Long> getUserNumberOfVotesQuery = this.entityManager.createQuery(query)
                .setParameter(voterUsernameParameter, user.getUsername());
        try {
            Long userNumberOfVotes = getUserNumberOfVotesQuery.getSingleResult();
            return userNumberOfVotes == null ? 0 : Math.toIntExact(userNumberOfVotes);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteUserVotes(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Vote> delete = builder.createCriteriaDelete(Vote.class);
        Root<Vote> vote = delete.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        delete
                .where(builder.equal(
                        vote.get(Vote_.voter).get(User_.username),
                        voterUsernameParameter
                ));

        this.entityManager.createQuery(delete)
                .setParameter(voterUsernameParameter, user.getUsername())
                .executeUpdate();
    }

    public List<Vote> getUserUpvotes(@NotNull final User user) {
        return this.getUserUpvotes(user, null, null);
    }

    public List<Vote> getUserUpvotes(@NotNull final User user,
                                     @Min(0) final Integer index,
                                     @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.voter).get(User_.username),
                                voterUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                +1
                        )
                ));

        TypedQuery<Vote> getUserUpvotesQuery = this.entityManager.createQuery(query)
                .setParameter(voterUsernameParameter, user.getUsername());
        List<Vote> userUpvotes = getUserUpvotesQuery.getResultList();
        return userUpvotes == null || userUpvotes.isEmpty() ? null : userUpvotes;
    }

    public @NotNull Integer getUserNumberOfUpvotes(@NotNull User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(vote))
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.voter).get(User_.username),
                                voterUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                +1
                        )
                ));

        TypedQuery<Long> getUserNumberOfUpvotesQuery = this.entityManager.createQuery(query)
                .setParameter(voterUsernameParameter, user.getUsername());
        try {
            Long userNumberOfUpvotes = getUserNumberOfUpvotesQuery.getSingleResult();
            return userNumberOfUpvotes == null ? 0 : Math.toIntExact(userNumberOfUpvotes);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteUserUpvotes(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Vote> delete = builder.createCriteriaDelete(Vote.class);
        Root<Vote> vote = delete.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        delete
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.voter).get(User_.username),
                                voterUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                +1
                        )
                ));

        this.entityManager.createQuery(delete)
                .setParameter(voterUsernameParameter, user.getUsername())
                .executeUpdate();
    }

    public List<Vote> getUserDownvotes(@NotNull final User user) {
        return this.getUserDownvotes(user, null, null);
    }

    public List<Vote> getUserDownvotes(@NotNull final User user,
                                       @Min(0) final Integer index,
                                       @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.voter).get(User_.username),
                                voterUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                -1
                        )
                ));

        TypedQuery<Vote> getUserDownvotesQuery = this.entityManager.createQuery(query)
                .setParameter(voterUsernameParameter, user.getUsername());
        List<Vote> userDownvotes = getUserDownvotesQuery.getResultList();
        return userDownvotes == null || userDownvotes.isEmpty() ? null : userDownvotes;
    }

    public @NotNull Integer getUserNumberOfDownvotes(@NotNull User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(vote))
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.voter).get(User_.username),
                                voterUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                -1
                        )
                ));

        TypedQuery<Long> getUserNumberOfDownvotesQuery = this.entityManager.createQuery(query)
                .setParameter(voterUsernameParameter, user.getUsername());
        try {
            Long userNumberOfDownvotes = getUserNumberOfDownvotesQuery.getSingleResult();
            return userNumberOfDownvotes == null ? 0 : Math.toIntExact(userNumberOfDownvotes);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteUserDownvotes(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Vote> delete = builder.createCriteriaDelete(Vote.class);
        Root<Vote> vote = delete.from(Vote.class);
        ParameterExpression<String> voterUsernameParameter = builder.parameter(String.class);
        delete
                .where(builder.and(
                        builder.equal(
                                vote.get(Vote_.voter).get(User_.username),
                                voterUsernameParameter
                        ),
                        builder.equal(
                                vote.get(Vote_.value),
                                -1
                        )
                ));

        this.entityManager.createQuery(delete)
                .setParameter(voterUsernameParameter, user.getUsername())
                .executeUpdate();
    }

    public List<BacklogEntry> getUserBacklog(@NotNull final User user) {
        return this.getUserBacklog(user, null, null);
    }

    public List<BacklogEntry> getUserBacklog(@NotNull final User user,
                                             @Min(0) final Integer index,
                                             @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<BacklogEntry> query = builder.createQuery(BacklogEntry.class);
        Root<BacklogEntry> backlogEntry = query.from(BacklogEntry.class);
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        query
                .select(backlogEntry)
                .where(builder.equal(
                        backlogEntry.get(BacklogEntry_.user).get(User_.username),
                        usernameParameter
                ))
                .orderBy(builder.desc(backlogEntry.get(BacklogEntry_.insertionTime)));

        TypedQuery<BacklogEntry> getUserBacklogQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        if (index != null)
            getUserBacklogQuery.setFirstResult(index);
        if (limit != null)
            getUserBacklogQuery.setMaxResults(limit);
        List<BacklogEntry> backlogEntries = getUserBacklogQuery.getResultList();
        return backlogEntries == null || backlogEntries.isEmpty() ? null : backlogEntries;
    }

    public Integer getUserBacklogLength(@NotNull User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<BacklogEntry> backlogEntry = query.from(BacklogEntry.class);
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(backlogEntry))
                .where(builder.equal(
                        backlogEntry.get(BacklogEntry_.user).get(User_.username),
                        usernameParameter
                ));

        TypedQuery<Long> getUserBacklogLengthQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        try {
            Long userBacklogLength = getUserBacklogLengthQuery.getSingleResult();
            return userBacklogLength == null ? 0 : Math.toIntExact(userBacklogLength);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void clearUserBacklog(@NotNull User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<BacklogEntry> delete = builder.createCriteriaDelete(BacklogEntry.class);
        Root<BacklogEntry> backlogEntry = delete.from(BacklogEntry.class);
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        delete
                .where(builder.equal(
                        backlogEntry.get(BacklogEntry_.user).get(User_.username),
                        usernameParameter
                ));

        this.entityManager.createQuery(delete)
                .setParameter(usernameParameter, user.getUsername())
                .executeUpdate();
    }

    public List<Report> getUserReports(@NotNull User user) {
        return this.getUserReports(user, null, null);
    }

    public List<Report> getUserReports(@NotNull User user,
                                       @Min(0) Integer index,
                                       @Min(1) Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Report> query = builder.createQuery(Report.class);
        Root<Report> report = query.from(Report.class);
        ParameterExpression<String> reporterUsernameParameter = builder.parameter(String.class);
        query
                .select(report)
                .where(builder.equal(
                        report.get(Report_.reporter).get(User_.username),
                        reporterUsernameParameter
                ));

        TypedQuery<Report> getUserReportsQuery = this.entityManager.createQuery(query)
                .setParameter(reporterUsernameParameter, user.getUsername());
        if (index != null)
            getUserReportsQuery.setFirstResult(index);
        if (limit != null)
            getUserReportsQuery.setMaxResults(limit);
        List<Report> userReports = getUserReportsQuery.getResultList();
        return userReports == null || userReports.isEmpty() ? null : userReports;
    }

    public @NotNull Integer getUserNumberOfReports(@NotNull User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Report> report = query.from(Report.class);
        ParameterExpression<String> reporterUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(report))
                .where(builder.equal(
                        report.get(Report_.reporter).get(User_.username),
                        reporterUsernameParameter
                ));

        TypedQuery<Long> getUserNumberOfReportsQuery = this.entityManager.createQuery(query)
                .setParameter(reporterUsernameParameter, user.getUsername());
        try {
            Long userNumberOfReports = getUserNumberOfReportsQuery.getSingleResult();
            return userNumberOfReports == null ? 0 : Math.toIntExact(userNumberOfReports);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public void deleteUserReports(@NotNull User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Report> delete = builder.createCriteriaDelete(Report.class);
        Root<Report> report = delete.from(Report.class);
        ParameterExpression<String> reporterUsernameParameter = builder.parameter(String.class);
        delete
                .where(builder.equal(
                        report.get(Report_.reporter).get(User_.username),
                        reporterUsernameParameter
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reporterUsernameParameter, user.getUsername())
                .executeUpdate();
    }

    public Double getUserAverageAssignedRating(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        query
                .select(builder.avg(review.get(Review_.rating)))
                .where(builder.equal(
                        review.get(Review_.reviewer).get(User_.username),
                        usernameParameter
                ));

        TypedQuery<Double> getUserAverageAssignedRatingQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        try {
            return getUserAverageAssignedRatingQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public @NotNull Integer getUserReputation(@NotNull final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        query
                .select(builder.sum(vote.get(Vote_.value)))
                .where(builder.equal(
                        vote.get(Vote_.review).get(Review_.reviewer).get(User_.username),
                        usernameParameter
                ));

        TypedQuery<Integer> getUserReputationQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        try {
            Integer userReputation = getUserReputationQuery.getSingleResult();
            return userReputation == null ? 0 : userReputation;
        } catch (NoResultException e) {
            return 0;
        }
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

    public void deleteReviewVotes(@NotNull Review review) {
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

    public void deleteReviewUpvotes(@NotNull Review review) {
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

    public void deleteReviewDownvotes(@NotNull Review review) {
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

    @Cacheable(type = "album")
    public Album getAlbum(@NotNull final Long albumId) {
        try {
            return this.client.getAlbum(albumId);
        } catch (DeezerClientException e) {
            if (e.getErrorCode().equals(DeezerClientException.DATA_NOT_FOUND))
                return null;
            throw e;
        }
    }

    @Cacheable(type = "artist")
    public Artist getArtist(@NotNull final Long artistId) {
        try {
            return this.client.getArtist(artistId);
        } catch (DeezerClientException e) {
            if (e.getErrorCode().equals(DeezerClientException.DATA_NOT_FOUND))
                return null;
            throw e;
        }
    }

    @Cacheable(type = "genre")
    public Genre getGenre(@NotNull final Long genreId) {
        try {
            return this.client.getGenre(genreId);
        } catch (DeezerClientException e) {
            if (e.getErrorCode().equals(DeezerClientException.DATA_NOT_FOUND))
                return null;
            throw e;
        }
    }

    public Genre getAlbumGenre(@NotNull final Album album) {
        Genres albumGenres = album.getGenres().getAsNullIfNoData();
        return albumGenres == null ? null : albumGenres.getData().get(0);
    }

    public List<Review> getAlbumReviews(@NotNull final Album album) {
        return this.getAlbumReviews(album, null, null);
    }

    public List<Review> getAlbumReviews(@NotNull final Album album,
                                        @Min(0) final Integer index,
                                        @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        Join<Review, Vote> vote = review.join(Review_.votes, JoinType.LEFT);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(review)
                .where(builder.equal(
                        review.get(Review_.reviewedAlbumId),
                        reviewedAlbumIdParameter
                ))
                .groupBy(review)
                .orderBy(builder.desc(builder.coalesce(
                        builder.sum(vote.get(Vote_.value)),
                        builder.literal(0)
                )));

        TypedQuery<Review> getTopReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewedAlbumIdParameter, album.getId());
        if (index != null)
            getTopReviewsQuery.setFirstResult(index);
        if (limit != null)
            getTopReviewsQuery.setMaxResults(limit);
        List<Review> topReviews = getTopReviewsQuery.getResultList();
        return topReviews == null || topReviews.isEmpty() ? null : topReviews;
    }

    public @NotNull Integer getAlbumNumberOfReviews(@NotNull final Album album) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.count(review))
                .where(builder.equal(
                        review.get(Review_.reviewedAlbumId),
                        reviewedAlbumIdParameter
                ));

        TypedQuery<Long> getAlbumNumberOfReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewedAlbumIdParameter, album.getId());
        try {
            Long albumNumberOfReviews = getAlbumNumberOfReviewsQuery.getSingleResult();
            return albumNumberOfReviews == null ? 0 : Math.toIntExact(albumNumberOfReviews);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public Double getAlbumAverageRating(@NotNull final Album album) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.avg(review.get(Review_.rating)))
                .where(builder.equal(
                        review.get(Review_.reviewedAlbumId),
                        reviewedAlbumIdParameter
                ));

        TypedQuery<Double> getAlbumAverageRatingQuery = this.entityManager.createQuery(query)
                .setParameter(reviewedAlbumIdParameter, album.getId());
        try {
            return getAlbumAverageRatingQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Cacheable(type = "artistAlbums")
    public Albums getArtistAlbums(@NotNull final Artist artist) {
        try {
            return this.client.getArtistAlbums(artist.getId(), 0, Integer.MAX_VALUE).getAsNullIfNoData();
        } catch (DeezerClientException e) {
            if (e.getErrorCode().equals(DeezerClientException.DATA_NOT_FOUND))
                return null;
            throw e;
        }
    }

    @Cacheable(type = "artistAlbums")
    public Albums getArtistAlbums(@NotNull final Artist artist,
                                  @NotNull @Min(0) final Integer index,
                                  @NotNull @Min(1) final Integer limit) {
        try {
            return this.client.getArtistAlbums(artist.getId(), index, limit).getAsNullIfNoData();
        } catch (DeezerClientException e) {
            if (e.getErrorCode().equals(DeezerClientException.DATA_NOT_FOUND))
                return null;
            throw e;
        }
    }

    public @NotNull Integer getArtistNumberOfReviews(@NotNull final Artist artist) {
        Albums artistAlbums = this.getArtistAlbums(artist);
        return artistAlbums == null
                ? 0
                : artistAlbums.getData().stream()
                .mapToInt(this::getAlbumNumberOfReviews)
                .sum();
    }

    public Double getArtistAverageRating(@NotNull final Artist artist) {
        Albums artistAlbums = this.getArtistAlbums(artist);
        OptionalDouble averageRating = artistAlbums == null
                ? OptionalDouble.empty()
                : artistAlbums.getData().stream()
                .filter(album -> this.getAlbumNumberOfReviews(album) != 0)
                .mapToDouble(this::getAlbumAverageRating)
                .average();
        return averageRating.isPresent() ? averageRating.getAsDouble() : null;
    }

    @Cacheable(type = "topAlbums")
    public Albums getTopAlbums() {
        return this.client.getTopAlbums(0, Integer.MAX_VALUE).getAsNullIfNoData();
    }

    @Cacheable(type = "topAlbums")
    public Albums getTopAlbums(@NotNull @Min(0) final Integer index,
                               @NotNull @Min(1) final Integer limit) {
        return this.client.getTopAlbums(index, limit).getAsNullIfNoData();
    }

    public Albums searchAlbums(@NotNull final String query) {
        AlbumsSearch albumsSearch = new AlbumsSearch(query);
        return this.client.getAlbumsSearchResults(albumsSearch, 0, Integer.MAX_VALUE).getAsNullIfNoData();
    }

    public Albums searchAlbums(@NotNull final String query,
                               @NotNull @Min(0) final Integer index,
                               @NotNull @Min(1) final Integer limit) {
        AlbumsSearch albumsSearch = new AlbumsSearch(query);
        return this.client.getAlbumsSearchResults(albumsSearch, index, limit).getAsNullIfNoData();
    }

    public Artists searchArtists(@NotNull final String query) {
        ArtistsSearch artistsSearch = new ArtistsSearch(query);
        return this.client.getArtistsSearchResults(artistsSearch, 0, Integer.MAX_VALUE).getAsNullIfNoData();
    }

    public Artists searchArtists(@NotNull final String query,
                                 @NotNull @Min(0) final Integer index,
                                 @NotNull @Min(1) final Integer limit) {
        ArtistsSearch artistsSearch = new ArtistsSearch(query);
        return this.client.getArtistsSearchResults(artistsSearch, index, limit).getAsNullIfNoData();
    }

}
