package model.transfer;

import model.access.LibraryAgent;
import model.access.UsersAgent;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Review implements Serializable {

    private static final long serialVersionUID = 1;

    private String reviewerUsername;
    private Long reviewedAlbumId;
    private String content;
    private Integer rating;
    private Date publicationDate;
    private List<Vote> votes;

    public String getReviewerUsername() {
        return this.reviewerUsername;
    }

    public Review setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
        return this;
    }

    public Long getReviewedAlbumId() {
        return this.reviewedAlbumId;
    }

    public Review setReviewedAlbumId(Long reviewedAlbumId) {
        this.reviewedAlbumId = reviewedAlbumId;
        return this;
    }

    public String getContent() {
        return this.content;
    }

    public Review setContent(String content) {
        this.content = content;
        return this;
    }

    public Integer getRating() {
        return this.rating;
    }

    public Review setRating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public Date getPublicationDate() {
        return this.publicationDate;
    }

    public Review setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
        return this;
    }

    public List<Vote> getVotes() {
        return this.votes;
    }

    public Review setVotes(List<Vote> votes) {
        this.votes = votes;
        return this;
    }

    public User getReviewer() {
        return UsersAgent.getUser(this.reviewerUsername);
    }

    public Album getReviewedAlbum() {
        return LibraryAgent.getAlbum(this.reviewedAlbumId);
    }

    public List<Vote> getUpvotes() {
        return UsersAgent.getReviewUpvotes(this);
    }

    public List<Vote> getDownvotes() {
        return UsersAgent.getReviewDownvotes(this);
    }

    public Integer getScore() {
        return UsersAgent.getReviewScore(this);
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Review.class.getSimpleName() + "{", "}")
                .add("reviewerUsername=" + (this.reviewerUsername == null ? null : "'" + this.reviewerUsername + "'"))
                .add("reviewedAlbumId=" + this.reviewedAlbumId)
                .add("content=" + (this.content == null ? null : "'" + this.content + "'"))
                .add("rating=" + this.rating)
                .add("publicationDate=" + this.publicationDate)
                .add("votes=" + this.votes)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) 
            return true;
        if (other == null || this.getClass() != other.getClass()) 
            return false;
        Review review = (Review) other;
        return  Objects.equals(this.reviewerUsername, review.reviewerUsername) &&
                Objects.equals(this.reviewedAlbumId, review.reviewedAlbumId) &&
                Objects.equals(this.content, review.content) &&
                Objects.equals(this.rating, review.rating) &&
                Objects.equals(this.publicationDate, review.publicationDate) &&
                Objects.equals(this.votes, review.votes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reviewerUsername, this.reviewedAlbumId, this.content, this.rating,
                            this.publicationDate, this.votes);
    }
    
}
