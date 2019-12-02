package application.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "vote")
@IdClass(Vote.VoteId.class)
public class Vote implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voterUsername", referencedColumnName = "username")
    @NotNull(message = "{vote.voter.NotNull}")
    private User voter;
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "reviewerUsername", referencedColumnName = "reviewerUsername"),
            @JoinColumn(name = "reviewedAlbumId", referencedColumnName = "reviewedAlbumId")
    })
    @NotNull(message = "{vote.review.NotNull}")
    private Review review;
    @Column(name = "value", nullable = false)
    @NotNull(message = "{vote.value.NotNull}")
    private Integer value;

    public User getVoter() {
        return this.voter;
    }

    public Vote setVoter(User voter) {
        this.voter = voter;
        return this;
    }

    public Review getReview() {
        return this.review;
    }

    public Vote setReview(Review review) {
        this.review = review;
        return this;
    }

    public Boolean getValue() {
        if (this.value == +1)
            return true;
        else if (this.value == -1)
            return false;
        return null;
    }

    public Vote setValue(Boolean value) {
        if (value == null)
            this.value = null;
        else
            this.value = value ? +1 : -1;
        return this;
    }

    public String getVoterUsername() {
        return this.voter.getUsername();
    }

    public Vote setVoterUsername(String voterUsername) {
        this.voter.setUsername(voterUsername);
        return this;
    }

    public String getReviewerUsername() {
        return this.review.getReviewerUsername();
    }

    public Vote setReviewerUsername(String reviewerUsername) {
        this.review.setReviewerUsername(reviewerUsername);
        return this;
    }

    public Long getReviewedAlbumId() {
        return this.review.getReviewedAlbumId();
    }

    public Vote setReviewedAlbumId(Long reviewedAlbumId) {
        this.review.setReviewedAlbumId(reviewedAlbumId);
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vote.class.getSimpleName() + "{", "}")
                .add("voterUsername=" + this.voter.getUsername())
                .add("reviewerUsername=" + this.review.getReviewerUsername())
                .add("reviewedAlbumId=" + this.review.getReviewedAlbumId())
                .add("value=" + this.value)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;
        Vote vote = (Vote) other;
        return  Objects.equals(this.voter, vote.voter) &&
                Objects.equals(this.review, vote.review) &&
                Objects.equals(this.value, vote.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.voter, this.review, this.value);
    }

    public static class VoteId implements Serializable {

        private static final long serialVersionUID = 1L;

        private String voter;
        private Review.ReviewId review;

        public String getVoterUsername() {
            return this.voter;
        }

        public VoteId setVoterUsername(String voterUsername) {
            this.voter = voterUsername;
            return this;
        }

        public Review.ReviewId getReviewId() {
            return this.review;
        }

        public VoteId setReviewId(Review.ReviewId reviewId) {
            this.review = reviewId;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", VoteId.class.getSimpleName() + "{", "}")
                    .add("voterUsername=" + this.voter)
                    .add("reviewerUsername=" + this.review.getReviewerUsername())
                    .add("reviewedAlbumId=" + this.review.getReviewedAlbumId())
                    .toString();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            if (object == null || this.getClass() != object.getClass())
                return false;
            VoteId voteId = (VoteId) object;
            return  Objects.equals(this.voter, voteId.voter) &&
                    Objects.equals(this.review, voteId.review);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.voter, this.review);
        }

    }

}
