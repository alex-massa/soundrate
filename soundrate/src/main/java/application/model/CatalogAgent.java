package application.model;

import application.entities.*;
import application.interceptors.bindings.Cacheable;
import application.model.exceptions.BacklogEntryNotFoundException;
import application.model.exceptions.ConflictingBacklogEntryException;
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
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.OptionalDouble;

@Stateless
public class CatalogAgent {

    @PersistenceContext
    private EntityManager entityManager;

    private DeezerClient client;

    public CatalogAgent() {
        this.client = new DeezerClient();
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
                .groupBy(review)
                .where(builder.equal(
                        review.get(Review_.reviewedAlbumId),
                        reviewedAlbumIdParameter
                ))
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

    public void deleteAlbumReviews(@NotNull final Album album) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaDelete<Review> delete = builder.createCriteriaDelete(Review.class);
        Root<Review> review = delete.from(Review.class);
        ParameterExpression<Long> reviewedAlbumIdParameter = builder.parameter(Long.class);
        delete
                .where(builder.equal(
                        review.get(Review_.reviewedAlbumId),
                        reviewedAlbumIdParameter
                ));

        this.entityManager.createQuery(delete)
                .setParameter(reviewedAlbumIdParameter, album.getId())
                .executeUpdate();
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
