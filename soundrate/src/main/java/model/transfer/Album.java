package model.transfer;

import deezer.model.data.Genres;
import model.access.LibraryAgent;

import java.io.Serializable;
import java.util.List;

public class Album extends deezer.model.Album implements Serializable {

    private static final long serialVersionUID = 1;

    public String getGenre() {
        Genres genres = this.getGenres().getAsNullIfNoData();
        if (genres == null)
            return null;
        return genres.getData().get(0).getName();
    }

    public List<Review> getReviews() {
        return LibraryAgent.getAlbumReviews(this);
    }

    public Integer getNumberOfReviews() {
        return LibraryAgent.getAlbumNumberOfReviews(this);
    }

    public Double getAverageRating() {
        return LibraryAgent.getAlbumAverageRating(this);
    }

}
