package application.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1;

    public static final int MIN_BIO_LENGTH = 1;
    public static final int MAX_BIO_LENGTH = 2500;

    public enum Role { USER, MODERATOR, ADMINISTRATOR }

    @Id
    @Column(name = "username")
    @NotNull(message = "{user.username.NotNull}")
    private String username;
    @Column(unique = true, nullable = false)
    @NotNull(message = "{user.email.NotNull}")
    @Email(message = "{user.email.Email}")
    private String email;
    @Column(name = "password", nullable = false)
    @NotNull(message = "{user.password.NotNull}")
    private String password;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="signUpDate", nullable = false)
    @NotNull(message = "{user.signUpDate.NotNull}")
    @PastOrPresent(message = "{user.signUpDate.PastOrPresent}")
    private Date signUpDate;
    @Column(name = "pictureUrl")
    private String picture;
    @Column(name = "bio", length = User.MAX_BIO_LENGTH)
    @Size(min = User.MIN_BIO_LENGTH, max = User.MAX_BIO_LENGTH, message = "{user.biography.Size}")
    private String biography;
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private User.Role role;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;
    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vote> votes;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BacklogEntry> backlog;

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
        try {
            return this.picture == null ? null : new URL(this.picture);
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

    public List<BacklogEntry> getBacklog() {
        return this.backlog;
    }

    public User setBacklog(List<BacklogEntry> backlog) {
        this.backlog = backlog;
        return this;
    }

    public User.Role getRole() {
        return this.role;
    }

    public User setRole(User.Role role) {
        this.role = role;
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
                .add("role=" + this.role)
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
                Objects.equals(this.role, user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.username, this.email, this.password,
                this.signUpDate, this.picture, this.biography, this.role);
    }

}
