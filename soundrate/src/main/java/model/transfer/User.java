package model.transfer;

import model.access.UsersAgent;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class User implements Serializable {

    private static final long serialVersionUID = 1;

    private String username;
    private String email;
    private String password;
    private Date signUpDate;
    private URL picture;
    private String biography;
    private List<Long> backlog;

    public String getUsername() {
        return this.username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return this.email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return this.password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public Date getSignUpDate() {
        return this.signUpDate;
    }

    public User setSignUpDate(Date signUpDate) {
        this.signUpDate = signUpDate;
        return this;
    }

    public URL getPicture() {
        return this.picture;
    }

    public User setPicture(URL picture) {
        this.picture = picture;
        return this;
    }

    public String getBiography() {
        return this.biography;
    }

    public User setBiography(String biography) {
        this.biography = biography;
        return this;
    }

    public List<Long> getBacklog() {
        return this.backlog;
    }

    public User setBacklog(List<Long> backlog) {
        this.backlog = backlog;
        return this;
    }

    public List<Review> getReviews() {
        return UsersAgent.getUserReviews(this);
    }

    public Integer getNumberOfReviews() {
        return UsersAgent.getUserNumberOfReviews(this);
    }

    public Double getAverageAssignedRating() {
        return UsersAgent.getUserAverageAssignedScore(this);
    }

    public Integer getReputation() {
        return UsersAgent.getUserReputation(this);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "{", "}")
                .add("username=" + (this.username == null ? null : "'" + this.username + "'"))
                .add("email=" + (this.email == null ? null : "'" + this.email + "'"))
                .add("password='" + (this.password == null ? null : "'" + this.password + "'"))
                .add("signUpDate=" + this.signUpDate)
                .add("picture=" + this.picture)
                .add("biography='" + (this.biography == null ? null : "'" + this.biography + "'"))
                .add("backlog=" + this.backlog)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;
        User user = (User) other;
        return  Objects.equals(this.username, user.username) &&
                Objects.equals(this.email, user.email) &&
                Objects.equals(this.password, user.password) &&
                Objects.equals(this.signUpDate, user.signUpDate) &&
                Objects.equals(this.picture, user.picture) &&
                Objects.equals(this.biography, user.biography) &&
                Objects.equals(this.backlog, user.backlog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.username, this.email, this.password, this.signUpDate, this.picture, this.biography,
                            this.backlog);
    }

}
