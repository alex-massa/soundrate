package application.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

@Entity @Table(name = "vote") @IdClass(Vote.VoteId.class)
public class Vote implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voterUsername", referencedColumnName = "username")
    private User voter;
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "reviewerUsername", referencedColumnName = "reviewerUsername"),
            @JoinColumn(name = "reviewedAlbumId", referencedColumnName = "reviewedAlbumId")
    })
    private Review review;
    @Column(name = "vote", nullable = false)
    private Integer vote;

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

    public Integer getVote() {
        return this.vote;
    }

    public Vote setVote(Integer vote) {
        this.vote = vote;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vote.class.getSimpleName() + "{", "}")
                .add("voter=" + this.voter)
                .add("review=" + this.review)
                .add("vote=" + this.vote)
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
                Objects.equals(this.vote, vote.vote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.voter, this.review, this.vote);
    }

    public static class VoteId implements Serializable {

        private static final long serialVersionUID = 1L;

        private String voter;
        private Review.ReviewId review;

        public String getVoterUsername() {
            return this.voter;
        }

        public VoteId setVoterUsername(String voter) {
            this.voter = voter;
            return this;
        }

        public Review.ReviewId getReviewId() {
            return this.review;
        }

        public VoteId setReviewId(Review.ReviewId review) {
            this.review = review;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", VoteId.class.getSimpleName() + "{", "}")
                    .add("voterUsername=" + (this.voter == null ? null : "'" + this.voter + "'"))
                    .add("reviewId=" + this.review)
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
