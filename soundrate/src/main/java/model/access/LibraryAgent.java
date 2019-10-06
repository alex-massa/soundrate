package model.access;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import deezer.client.DeezerClient;
import deezer.model.data.Albums;
import deezer.model.data.Artists;
import deezer.model.search.AlbumsSearch;
import deezer.model.search.ArtistsSearch;
import model.transfer.Album;
import model.transfer.Artist;
import model.transfer.Review;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public final class LibraryAgent {

    private LibraryAgent() {
        throw new UnsupportedOperationException();
    }

    private static DeezerClient apiClient = new DeezerClient();
    private static Gson serializer = new Gson();

    public static Album getAlbum(final long albumId) {
        return serializer.fromJson(serializer.toJson(LibraryAgent.apiClient.getAlbum(albumId)), Album.class);
    }

    public static Artist getArtist(final long artistId) {
        return serializer.fromJson(serializer.toJson(LibraryAgent.apiClient.getArtist(artistId)), Artist.class);
    }

    public static String getGenre(final long genreId) {
        return apiClient.getGenre(genreId).getName();
    }

    public static List<Review> getAlbumReviews(final Album album) {
        return UsersAgent.getReviews().parallelStream()
                .filter(review -> review.getReviewedAlbumId().equals(album.getId()))
                .sorted(Comparator.comparing(UsersAgent::getReviewScore).reversed())
                .collect(Collectors.toList());
    }

    public static int getAlbumNumberOfReviews(final Album album) {
        List<Review> reviews = LibraryAgent.getAlbumReviews(album);
        return (reviews == null ? 0 : reviews.size());
    }

    public static Double getAlbumAverageRating(final Album album) {
        List<Review> reviews = LibraryAgent.getAlbumReviews(album);
        OptionalDouble averageRating = reviews == null
                ? OptionalDouble.empty()
                : reviews.parallelStream()
                .mapToDouble(Review::getRating)
                .average();
        return averageRating.isPresent() ? averageRating.getAsDouble() : null;
    }

    public static List<Album> getArtistAlbums(final Artist artist) {
        Albums albums = apiClient.getArtistAlbums(artist.getId()).getAsNullIfNoData();
        if (albums == null)
            return null;
        Type listType = new TypeToken<List<Album>>(){}.getType();
        return serializer.fromJson(serializer.toJson(albums.getData()), listType);
    }

    public static int getArtistNumberOfReviews(final Artist artist) {
        List<Album> albums = LibraryAgent.getArtistAlbums(artist);
        return albums == null
                ? 0
                : albums.parallelStream()
                .mapToInt(LibraryAgent::getAlbumNumberOfReviews)
                .sum();
    }

    public static Double getArtistAverageRating(final Artist artist) {
        List<Album> albums = LibraryAgent.getArtistAlbums(artist);
        OptionalDouble averageRating =
                albums == null
                ? OptionalDouble.empty()
                : albums.parallelStream()
                .filter(album -> LibraryAgent.getAlbumNumberOfReviews(album) != 0)
                .mapToDouble(LibraryAgent::getAlbumAverageRating)
                .average();
        return averageRating.isPresent() ? averageRating.getAsDouble() : null;
    }

    public static List<Album> getTopAlbums() {
        Albums topAlbums = LibraryAgent.apiClient.getTopAlbums().getAsNullIfNoData();
        if (topAlbums == null)
            return null;
        Type listType = new TypeToken<List<Album>>(){}.getType();
        return serializer.fromJson(serializer.toJson(topAlbums.getData()), listType);
    }

    public static List<Album> getTopAlbums(final int index, final int limit) {
        Albums topAlbums = LibraryAgent.apiClient.getTopAlbums(index, limit).getAsNullIfNoData();
        if (topAlbums == null)
            return null;
        Type listType = new TypeToken<List<Album>>(){}.getType();
        return serializer.fromJson(serializer.toJson(topAlbums.getData()), listType);
    }

    public static List<Album> searchAlbums(final String query) {
        AlbumsSearch albumsSearch = new AlbumsSearch(query);
        Albums albumsSearchResults = LibraryAgent.apiClient.getAlbumsSearchResults(albumsSearch).getAsNullIfNoData();
        if (albumsSearchResults == null)
            return null;
        List<deezer.model.Album> albums = albumsSearchResults.getData();
        Type listType = new TypeToken<List<Album>>(){}.getType();
        return serializer.fromJson(serializer.toJson(albums), listType);
    }

    public static List<Artist> searchArtists(final String query) {
        ArtistsSearch artistsSearch = new ArtistsSearch(query);
        Artists artistsSearchResults = LibraryAgent.apiClient.getArtistsSearchResults(artistsSearch).getAsNullIfNoData();
        if (artistsSearchResults == null)
            return null;
        List<deezer.model.Artist> artists = artistsSearchResults.getData();
        Type listType = new TypeToken<List<Artist>>(){}.getType();
        return serializer.fromJson(serializer.toJson(artists), listType);
    }

}
