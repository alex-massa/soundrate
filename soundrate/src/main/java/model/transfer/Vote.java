package model.transfer;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class Vote implements Serializable {

    private static final long serialVersionUID = 1;

    private String voterUsername;
    private String reviewerUsername;
    private Long reviewedAlbumId;
    private Boolean vote;

    public String getVoterUsername() {
        return this.voterUsername;
    }

    public Vote setVoterUsername(String voterUsername) {
        this.voterUsername = voterUsername;
        return this;
    }

    public String getReviewerUsername() {
        return this.reviewerUsername;
    }

    public Vote setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
        return this;
    }

    public Long getReviewedAlbumId() {
        return this.reviewedAlbumId;
    }

    public Vote setReviewedAlbumId(Long reviewedAlbumId) {
        this.reviewedAlbumId = reviewedAlbumId;
        return this;
    }

    public Boolean getVote() {
        return this.vote;
    }

    public Vote setVote(Boolean vote) {
        this.vote = vote;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vote.class.getSimpleName() + "{", "}")
                .add("voterUsername=" + (this.voterUsername == null ? null : "'" + this.voterUsername + "'"))
                .add("reviewerUsername=" + (this.reviewerUsername == null ? null : "'" + this.reviewerUsername + "'"))
                .add("reviewedAlbumId=" + this.reviewedAlbumId)
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
        return  Objects.equals(this.voterUsername, vote.voterUsername) &&
                Objects.equals(this.reviewerUsername, vote.reviewerUsername) &&
                Objects.equals(this.reviewedAlbumId, vote.reviewedAlbumId) &&
                Objects.equals(this.vote, vote.vote);
    }



    @Override
    public int hashCode() {
        return Objects.hash(this.voterUsername, this.reviewerUsername, this.reviewedAlbumId, this.vote);
    }

}
