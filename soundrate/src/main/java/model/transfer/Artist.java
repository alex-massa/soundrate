package model.transfer;

import model.access.LibraryAgent;

import java.io.Serializable;
import java.util.List;

public class Artist extends deezer.model.Artist implements Serializable {

    private static final long serialVersionUID = 1;

    public List<Album> getAlbums() {
        return LibraryAgent.getArtistAlbums(this);
    }

    public Integer getNumberOfReviews() {
        return LibraryAgent.getArtistNumberOfReviews(this);
    }

    public Double getAverageRating() {
        return LibraryAgent.getArtistAverageRating(this);
    }

}
