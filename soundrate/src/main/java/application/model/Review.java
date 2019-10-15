package application.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Entity @Table(name = "review") @IdClass(Review.ReviewId.class)
public class Review implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewerUsername", referencedColumnName = "username")
    private User reviewer;
    @Id
    @Column(name = "reviewedAlbumId")
    private Long reviewedAlbumId;
    @Column(name = "content", nullable = false, length = 2500)
    private String content;
    @Column(name = "rating", nullable = false)
    private Integer rating;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publicationDate", nullable = false)
    private Date publicationDate;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vote> votes;

    public User getReviewer() {
        return this.reviewer;
    }

    public Review setReviewer(User reviewer) {
        this.reviewer = reviewer;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", Review.class.getSimpleName() + "{", "}")
                .add("reviewer=" + this.reviewer)
                .add("reviewedAlbumId=" + this.reviewedAlbumId)
                .add("content=" + (this.content == null ? null : "'" + this.content + "'"))
                .add("rating=" + this.rating)
                .add("publicationDate=" + this.publicationDate)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) 
            return true;
        if (other == null || this.getClass() != other.getClass()) 
            return false;
        Review review = (Review) other;
        return  Objects.equals(this.reviewer, review.reviewer) &&
                Objects.equals(this.reviewedAlbumId, review.reviewedAlbumId) &&
                Objects.equals(this.content, review.content) &&
                Objects.equals(this.rating, review.rating) &&
                Objects.equals(this.publicationDate, review.publicationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reviewer, this.reviewedAlbumId, this.content, this.rating,
                            this.publicationDate);
    }

    public static class ReviewId implements Serializable {

        private static final long serialVersionUID = 1L;

        private String reviewer;
        private Long reviewedAlbumId;

        public String getReviewerUsername() {
            return this.reviewer;
        }

        public ReviewId setReviewerUsername(String reviewer) {
            this.reviewer = reviewer;
            return this;
        }

        public Long getReviewedAlbumId() {
            return this.reviewedAlbumId;
        }

        public ReviewId setReviewedAlbumId(Long reviewedAlbumId) {
            this.reviewedAlbumId = reviewedAlbumId;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ReviewId.class.getSimpleName() + "{", "}")
                    .add("reviewerUsername=" + (this.reviewer == null ? null : "'" + this.reviewer + "'"))
                    .add("reviewedAlbumId=" + this.reviewedAlbumId)
                    .toString();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || this.getClass() != other.getClass())
                return false;
            ReviewId reviewId = (ReviewId) other;
            return  Objects.equals(this.reviewer, reviewId.reviewer) &&
                    Objects.equals(this.reviewedAlbumId, reviewId.reviewedAlbumId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.reviewer, this.reviewedAlbumId);
        }

    }

}
