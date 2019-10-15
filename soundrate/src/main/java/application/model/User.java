package application.model;

import javax.persistence.*;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Entity @Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @Column(name = "username")
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="signUpDate", nullable = false)
    private Date signUpDate;
    @Column(name = "pictureUrl")
    private String picture;
    @Column(name = "bio", length = 2500)
    private String biography;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "backlogEntry", joinColumns = @JoinColumn(name = "username"))
    @Column(name = "albumId")
    private List<Long> backlog;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;
    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vote> votes;

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
        if (this.picture == null)
            return null;
        try {
            return new URL(this.picture);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public User setPicture(URL picture) {
        this.picture = picture.toString();
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
        return this.reviews;
    }

    public User setReviews(List<Review> reviews) {
        this.reviews = reviews;
        return this;
    }

    public List<Vote> getVotes() {
        return this.votes;
    }

    public User setVotes(List<Vote> votes) {
        this.votes = votes;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "{", "}")
                .add("username=" + (this.username == null ? null : "'" + this.username + "'"))
                .add("email=" + (this.email == null ? null : "'" + this.email + "'"))
                .add("password=" + (this.password == null ? null : "'" + this.password + "'"))
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
