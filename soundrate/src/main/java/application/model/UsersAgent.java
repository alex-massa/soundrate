package application.model;

import application.entities.*;
import application.interceptors.bindings.UserUpdate;
import application.model.exceptions.ConflictingEmailAddressException;
import application.model.exceptions.ConflictingUsernameException;
import application.model.exceptions.UserNotFoundException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Stateless
public class UsersAgent {

    @PersistenceContext
    private EntityManager entityManager;

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

}
