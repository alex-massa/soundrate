package application.business;

import application.exceptions.ConflictingEmailAddressException;
import application.exceptions.ConflictingUsernameException;
import application.model.*;
import deezer.client.DeezerClient;
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
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.LinkedList;
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

    public User getUser(final String username) {
        try {
            return this.entityManager.find(User.class, username);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public User getUserByEmail(final String email) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> user = query.from(User.class);
        ParameterExpression<String> emailParameter = builder.parameter(String.class);
        query
                .select(user)
                .where(builder.equal(user.get(User_.email), emailParameter));

        TypedQuery<User> getUserByEmailQuery = this.entityManager.createQuery(query)
                .setParameter(emailParameter, email);
        try {
            return getUserByEmailQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Review getReview(final String reviewerUsername, final long reviewedAlbumId) {
        Review.ReviewId reviewId = new Review.ReviewId()
                .setReviewerUsername(reviewerUsername)
                .setReviewedAlbumId(reviewedAlbumId);
        try {
            return this.entityManager.find(Review.class, reviewId);
        } catch (NoResultException e) {
            return null;
        }
    }

    public Vote getVote(final String voterUsername, final String reviewerUsername, final long reviewedAlbumId) {
        Review.ReviewId reviewId = new Review.ReviewId()
                .setReviewedAlbumId(reviewedAlbumId)
                .setReviewerUsername(reviewerUsername);
        Vote.VoteId voteId = new Vote.VoteId()
                .setVoterUsername(voterUsername)
                .setReviewId(reviewId);
        try {
            return this.entityManager.find(Vote.class, voteId);
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Review> getUserReviews(final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        query
                .select(review)
                .where(builder.equal(review.get(Review_.reviewer).get(User_.username), reviewerUsernameParameter))
                .orderBy(builder.desc(review.get(Review_.publicationDate)));

        TypedQuery<Review> getUserReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, user.getUsername());
        List<Review> userReviews = getUserReviewsQuery.getResultList();
        return userReviews != null && userReviews.isEmpty() ? null : userReviews;
    }

    public int getUserNumberOfReviews(final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.count(review))
                .where(builder.equal(review.get(Review_.reviewer).get(User_.username), reviewerUsernameParameter));

        TypedQuery<Long> getUserNumberOfReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, user.getUsername());
        try {
            Long userNumberOfReviews = getUserNumberOfReviewsQuery.getSingleResult();
            return userNumberOfReviews == null ? 0 : Math.toIntExact(userNumberOfReviews);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public Double getUserAverageAssignedRating(final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.avg(review.get(Review_.rating)))
                .where(builder.equal(review.get(Review_.reviewer).get(User_.username), reviewerUsernameParameter));

        TypedQuery<Double> getUserAverageAssignedRatingQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, user.getUsername());
        try {
            return getUserAverageAssignedRatingQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Album> getAlbumsInUserBacklog(User user) {
        if (!this.entityManager.contains(user))
            user = this.entityManager.merge(user);
        List<Album> userBacklog = user.getBacklog() == null
                ? null
                : user.getBacklog().parallelStream()
                .map(this::getAlbum)
                .collect(Collectors.toList());
        return userBacklog != null && userBacklog.isEmpty() ? null : userBacklog;
    }

    public int getUserReputation(final User user) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        query
                .select(builder.sum(vote.get(Vote_.vote)))
                .where(builder.equal(vote.get(Vote_.review).get(Review_.reviewer).get(User_.username), reviewerUsernameParameter));

        TypedQuery<Integer> getUserReputationQuery = this.entityManager.createQuery(query)
                .setParameter(reviewerUsernameParameter, user.getUsername());
        try {
            Integer userReputation = getUserReputationQuery.getSingleResult();
            return userReputation == null ? 0 : userReputation;
        } catch (NoResultException e) {
            return 0;
        }
    }

    public List<Vote> getReviewVotes(final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewer).get(User_.username), reviewerUsernameParameter),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewedAlbumId), reviewedAlbumIdParameter)
                ));

        TypedQuery<Vote> getReviewVotesQuery = this.entityManager.createQuery(query);
        getReviewVotesQuery
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        List<Vote> reviewVotes = getReviewVotesQuery.getResultList();
        return reviewVotes != null && reviewVotes.isEmpty() ? null : reviewVotes;
    }

    public List<Vote> getReviewUpvotes(final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(vote.get(Vote_.vote), +1),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewer).get(User_.username), reviewerUsernameParameter),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewedAlbumId), reviewedAlbumIdParameter)
                ));

        TypedQuery<Vote> getReviewUpvotesQuery = this.entityManager.createQuery(query);
        getReviewUpvotesQuery
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        List<Vote> reviewUpvotes = getReviewUpvotesQuery.getResultList();
        return reviewUpvotes != null && reviewUpvotes.isEmpty() ? null : reviewUpvotes;
    }

    public List<Vote> getReviewDownvotes(final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(vote.get(Vote_.vote), -1),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewer).get(User_.username), reviewerUsernameParameter),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewedAlbumId), reviewedAlbumIdParameter)
                ));

        TypedQuery<Vote> getReviewDownvotesQuery = this.entityManager.createQuery(query);
        getReviewDownvotesQuery
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        List<Vote> reviewDownvotes = getReviewDownvotesQuery.getResultList();
        return reviewDownvotes != null && reviewDownvotes.isEmpty() ? null : reviewDownvotes;
    }

    public int getReviewScore(final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.sum(vote.get(Vote_.vote)))
                .where(builder.and(
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewer).get(User_.username), reviewerUsernameParameter),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewedAlbumId), reviewedAlbumIdParameter)
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
        return this.getTopReviews(0, 10);
    }

    public List<Review> getTopReviews(final int index, final int limit) {
        if (index < 0 || limit < 0)
            throw new IllegalArgumentException();
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        Join<Review, Vote> vote = review.join(Review_.votes, JoinType.LEFT);
        query
                .select(review)
                .groupBy(review)
                .orderBy(builder.desc(builder.coalesce(builder.sum(vote.get(Vote_.vote)), 0)));

        TypedQuery<Review> getTopReviewsQuery = this.entityManager.createQuery(query)
                .setFirstResult(index)
                .setMaxResults(limit);
        List<Review> topReviews = getTopReviewsQuery.getResultList();
        return topReviews != null && topReviews.isEmpty() ? null : topReviews;
    }

    public Vote getUserReviewVote(final User voter, final Review review) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Vote> query = builder.createQuery(Vote.class);
        Root<Vote> vote = query.from(Vote.class);
        ParameterExpression<User> voterParameter = builder.parameter(User.class);
        ParameterExpression<String> reviewerUsernameParameter = builder.parameter(String.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(vote)
                .where(builder.and(
                        builder.equal(vote.get(Vote_.voter), voterParameter),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewer).get(User_.username), reviewerUsernameParameter),
                        builder.equal(vote.get(Vote_.review).get(Review_.reviewedAlbumId), reviewedAlbumIdParameter)
                ));

        TypedQuery<Vote> getUserReviewVote = this.entityManager.createQuery(query);
        getUserReviewVote
                .setParameter(voterParameter, voter)
                .setParameter(reviewerUsernameParameter, review.getReviewer().getUsername())
                .setParameter(reviewedAlbumIdParameter, review.getReviewedAlbumId());
        try {
            return getUserReviewVote.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Boolean areUserCredentialsValid(final String username, final String password) {
        User user = this.getUser(username);
        return user != null && user.getPassword().equals(password);
    }

    public void registerUser(final User user) throws ConflictingUsernameException, ConflictingEmailAddressException {
        if (this.getUser(user.getUsername()) != null)
            throw new ConflictingUsernameException();
        if (this.getUserByEmail(user.getEmail()) != null)
            throw new ConflictingEmailAddressException();
        this.entityManager.persist(user);
    }

    public void voteReview(final User voter, final Review review, final Boolean value) {
        Vote userVote = this.getUserReviewVote(voter, review);
        if (userVote == null) {
            if (value != null) {
                userVote = new Vote()
                        .setVoter(voter)
                        .setReview(review)
                        .setVote(value ? +1 : -1);
                this.entityManager.persist(userVote);
            }
        } else {
            if (value != null) {
                userVote.setVote(value ? +1 : -1);
                this.entityManager.merge(userVote);
            }
            else {
                if (!this.entityManager.contains(userVote))
                    userVote = this.entityManager.merge(userVote);
                this.entityManager.remove(userVote);
            }
        }
    }

    public boolean isAlbumInUserBacklog(User user, final Album album) {
        if (!this.entityManager.contains(user))
            user = this.entityManager.merge(user);
        List<Long> backlog = user.getBacklog();
        return backlog != null && backlog.contains(album.getId());
    }

    public void insertAlbumInUserBacklog(User user, final Album album) {
        if (!this.entityManager.contains(user))
            user = this.entityManager.merge(user);
        List<Long> backlog = user.getBacklog();
        if (backlog == null) {
            backlog = new LinkedList<>();
            user.setBacklog(backlog);
        }
        backlog.add(album.getId());
        this.entityManager.merge(user);
    }

    public void removeAlbumFromUserBacklog(User user, final Album album) {
        if (!this.entityManager.contains(user))
            user = this.entityManager.merge(user);
        List<Long> backlog = user.getBacklog();
        if (backlog == null || !backlog.contains(album.getId()))
            return;
        backlog.remove(album.getId());
        if (backlog.isEmpty())
            user.setBacklog(null);
        this.entityManager.merge(user);
    }

    public void publishReview(final Review toPublish) {
        this.entityManager.persist(toPublish);
    }

    public void editReview(Review toEdit, final String content, final int rating) {
        if (rating <= 0 || rating > 10)
            throw new IllegalArgumentException();
        if (!this.entityManager.contains(toEdit))
            toEdit = this.entityManager.merge(toEdit);
        toEdit.setRating(rating).setContent(content);
        this.entityManager.merge(toEdit);
    }

    public void deleteReview(Review review) {
        if (!this.entityManager.contains(review))
            review = this.entityManager.merge(review);
        this.entityManager.remove(review);
    }

    /* Library data methods */

    public Album getAlbum(final long albumId) {
        return this.client.getAlbum(albumId);
    }

    public Artist getArtist(final long artistId) {
        return this.client.getArtist(artistId);
    }

    public Genre getGenre(final long genreId) {
        return this.client.getGenre(genreId);
    }

    public Genre getAlbumGenre(final Album album) {
        Genres genres = album.getGenres().getAsNullIfNoData();
        if (genres == null)
            return null;
        return genres.getData().get(0);
    }

    public List<Review> getAlbumReviews(final Album album) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = builder.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);
        Join<Review, Vote> vote = review.join(Review_.votes, JoinType.LEFT);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(review)
                .where(builder.equal(review.get(Review_.reviewedAlbumId), reviewedAlbumIdParameter))
                .groupBy(review)
                .orderBy(builder.desc(builder.coalesce(builder.sum(vote.get(Vote_.vote)), 0)));

        TypedQuery<Review> getTopReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewedAlbumIdParameter, album.getId());
        List<Review> topReviews = getTopReviewsQuery.getResultList();
        return topReviews != null && topReviews.isEmpty() ? null : topReviews;
    }

    public int getAlbumNumberOfReviews(final Album album) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.count(review))
                .where(builder.equal(review.get(Review_.reviewedAlbumId), reviewedAlbumIdParameter));

        TypedQuery<Long> getAlbumNumberOfReviewsQuery = this.entityManager.createQuery(query)
                .setParameter(reviewedAlbumIdParameter, album.getId());
        try {
            Long albumNumberOfReviews = getAlbumNumberOfReviewsQuery.getSingleResult();
            return albumNumberOfReviews == null ? 0 : Math.toIntExact(albumNumberOfReviews);
        } catch (NoResultException e) {
            return 0;
        }
    }

    public Double getAlbumAverageRating(final Album album) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Review> review = query.from(Review.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        query
                .select(builder.avg(review.get(Review_.rating)))
                .where(builder.equal(review.get(Review_.reviewedAlbumId), reviewedAlbumIdParameter));

        TypedQuery<Double> getAlbumAverageRatingQuery = this.entityManager.createQuery(query)
                .setParameter(reviewedAlbumIdParameter, album.getId());
        try {
            return getAlbumAverageRatingQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Album> getArtistAlbums(final Artist artist) {
        Albums artistAlbums = this.client.getArtistAlbums(artist.getId()).getAsNullIfNoData();
        if (artistAlbums == null)
            return null;
        return artistAlbums.getData();
    }

    public int getArtistNumberOfReviews(final Artist artist) {
        List<Album> artistAlbums = this.getArtistAlbums(artist);
        return artistAlbums == null
                ? 0
                : artistAlbums.parallelStream()
                .mapToInt(this::getAlbumNumberOfReviews)
                .sum();
    }

    public Double getArtistAverageRating(final Artist artist) {
        List<Album> artistAlbums = this.getArtistAlbums(artist);
        OptionalDouble averageRating = artistAlbums == null
                ? OptionalDouble.empty()
                : artistAlbums.parallelStream()
                .filter(album -> this.getAlbumNumberOfReviews(album) != 0)
                .mapToDouble(this::getAlbumAverageRating)
                .average();
        return averageRating.isPresent() ? averageRating.getAsDouble() : null;
    }

    public List<Album> getTopAlbums() {
        return this.getTopAlbums(0, 100);
    }

    public List<Album> getTopAlbums(final int index, final int limit) {
        Albums topAlbums = this.client.getTopAlbums(index, limit).getAsNullIfNoData();
        if (topAlbums == null)
            return null;
        return topAlbums.getData();
    }

    public List<Album> searchAlbums(final String query) {
        AlbumsSearch albumsSearch = new AlbumsSearch(query);
        Albums albumsSearchResults = this.client
                .getAlbumsSearchResults(albumsSearch).getAsNullIfNoData();
        if (albumsSearchResults == null)
            return null;
        return albumsSearchResults.getData();
    }

    public List<Album> searchAlbums(final String query, final int index, final int limit) {
        AlbumsSearch albumsSearch = new AlbumsSearch(query);
        Albums albumsSearchResults = this.client
                .getAlbumsSearchResults(albumsSearch, index, limit).getAsNullIfNoData();
        if (albumsSearchResults == null)
            return null;
        return albumsSearchResults.getData();
    }

    public List<Artist> searchArtists(final String query) {
        ArtistsSearch artistsSearch = new ArtistsSearch(query);
        Artists artistsSearchResults = this.client
                .getArtistsSearchResults(artistsSearch).getAsNullIfNoData();
        if (artistsSearchResults == null)
            return null;
        return artistsSearchResults.getData();
    }

    public List<Artist> searchArtists(final String query, final int index, final int limit) {
        ArtistsSearch artistsSearch = new ArtistsSearch(query);
        Artists artistsSearchResults = this.client
                .getArtistsSearchResults(artistsSearch, index, limit).getAsNullIfNoData();
        if (artistsSearchResults == null)
            return null;
        return artistsSearchResults.getData();
    }

}
