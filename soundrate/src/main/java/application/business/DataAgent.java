package application.business;

import application.exceptions.*;
import application.interceptors.bindings.Cacheable;
import application.model.*;
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
import java.util.stream.Collectors;

@Stateless
public class DataAgent {

    @PersistenceContext(unitName = "soundratePersistenceUnit")
    private EntityManager entityManager;

    @Inject
    private DeezerClient client;

    /* Users data methods */

    public List<User> getUsers() {
        return this.getUsers(null, null);
    }

    public List<User> getUsers(@Min(0) Integer index, @Min(1) Integer limit) {
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

    public List<BacklogEntry> getBacklogEntries() {
        return this.getBacklogEntries(null, null);
    }

    public List<BacklogEntry> getBacklogEntries(@Min(0) Integer index, @Min(1) Integer limit) {
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

    public List<Review> getReviews() {
        return this.getReviews(null, null);
    }

    public List<Review> getReviews(@Min(0) Integer index, @Min(1) Integer limit) {
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

    public List<Vote> getVotes() {
        return this.getVotes(null, null);
    }

    public List<Vote> getVotes(@Min(0) Integer index, @Min(1) Integer limit) {
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

    public List<Review> getUserReviews(@NotNull final User user) {
        return this.getUserReviews(user, null, null);
    }

    public List<Review> getUserReviews(@NotNull final User user,
                                       @Min(0) final Integer index,
                                       @Min(1) final Integer limit) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        query
                .select(review)
                .where(builder.equal(
                        review.get(Review_.reviewer).get(User_.username),
                        usernameParameter
                ))
                .orderBy(builder.desc(review.get(Review_.publicationDate)));

        TypedQuery<Review> getUserReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
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
        ParameterExpression<String> usernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(review))
                .where(builder.equal(
                        review.get(Review_.reviewer).get(User_.username),
                        usernameParameter
                ));

        TypedQuery<Long> getUserNumberOfReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        try {
            Long userNumberOfReviews = getUserNumberOfReviewsQuery.getSingleResult();
            return userNumberOfReviews == null ? 0 : Math.toIntExact(userNumberOfReviews);
        } catch (NoResultException e) {
            return 0;
        }
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

    public List<Album> getAlbumsInUserBacklog(@NotNull final User user) {
        return this.getAlbumsInUserBacklog(user, null, null);
    }

    public List<Album> getAlbumsInUserBacklog(@NotNull final User user,
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

        TypedQuery<BacklogEntry> getAlbumsInUserBacklogQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        if (index != null)
            getAlbumsInUserBacklogQuery.setFirstResult(index);
        if (limit != null)
            getAlbumsInUserBacklogQuery.setMaxResults(limit);
        List<BacklogEntry> backlogEntries = getAlbumsInUserBacklogQuery.getResultList();
        return backlogEntries == null || backlogEntries.isEmpty()
                ? null
                : backlogEntries.stream().map
                (entry -> this.getAlbum(entry.getAlbumId())).collect(Collectors.toList());
    }

    public Integer getAlbumsNumberInUserBacklog(@NotNull User user) {
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

        TypedQuery<Long> getAlbumsNumberInUserBacklogQuery = this.entityManager.createQuery(query)
                .setParameter(usernameParameter, user.getUsername());
        try {
            Long albumsNumberInUserBacklog = getAlbumsNumberInUserBacklogQuery.getSingleResult();
            return albumsNumberInUserBacklog == null ? 0 : Math.toIntExact(albumsNumberInUserBacklog);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public List<Vote> getReviewVotes(@NotNull final Review review) {
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
        List<Vote> reviewVotes = getReviewVotesQuery.getResultList();
        return reviewVotes == null || reviewVotes.isEmpty() ? null : reviewVotes;
    }

    public List<Vote> getReviewUpvotes(@NotNull final Review review) {
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
        List<Vote> reviewUpvotes = getReviewUpvotesQuery.getResultList();
        return reviewUpvotes == null || reviewUpvotes.isEmpty() ? null : reviewUpvotes;
    }

    public List<Vote> getReviewDownvotes(@NotNull final Review review) {
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
        List<Vote> reviewDownvotes = getReviewDownvotesQuery.getResultList();
        return reviewDownvotes == null || reviewDownvotes.isEmpty() ? null : reviewDownvotes;
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

    public void createUser(@NotNull final User user) {
        if (this.getUser(user.getUsername()) != null)
            throw new ConflictingUsernameException();
        if (this.getUserByEmail(user.getEmail()) != null)
            throw new ConflictingEmailAddressException();
        this.entityManager.persist(user);
    }

    public void updateUser(@NotNull final User user) {
        if (this.getUser(user.getUsername()) == null)
            throw new UserNotFoundException();
        User other = this.getUserByEmail(user.getEmail());
        if (other != null && !user.getUsername().equals(other.getUsername()))
            throw new ConflictingEmailAddressException();
        this.entityManager.merge(user);
    }

    public void deleteUser(@NotNull User user) {
        if (this.getUser(user.getUsername()) == null)
            throw new UserNotFoundException();
        if (!this.entityManager.contains(user))
            user = this.entityManager.merge(user);
        this.entityManager.remove(user);
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

    /* Library data methods */

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
        Genres genres = album.getGenres().getAsNullIfNoData();
        return genres == null ? null : genres.getData().get(0);
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
