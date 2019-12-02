package application.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "backlogEntry")
@IdClass(BacklogEntry.BacklogEntryId.class)
public class BacklogEntry implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username")
    @NotNull(message = "{backlogEntry.user.NotNull}")
    private User user;
    @Id
    @Column(name = "albumId")
    @NotNull(message = "{backlogEntry.albumId.NotNull}")
    private Long albumId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "insertionName", nullable = false)
    @NotNull(message = "{backlogEntry.insertionTime.NotNull}")
    @PastOrPresent(message = "{backlogEntry.insertionTime.PastOrPresent}")
    private Date insertionTime;

    public User getUser() {
        return this.user;
    }

    public BacklogEntry setUser(User user) {
        this.user = user;
        return this;
    }

    public Long getAlbumId() {
        return this.albumId;
    }

    public BacklogEntry setAlbumId(Long albumId) {
        this.albumId = albumId;
        return this;
    }

    public Date getInsertionTime() {
        return insertionTime;
    }

    public BacklogEntry setInsertionTime(Date insertionTime) {
        this.insertionTime = insertionTime;
        return this;
    }

    public String getUsername() {
        return this.user.getUsername();
    }

    public BacklogEntry setUsername(String username) {
        this.user.setUsername(username);
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BacklogEntry.class.getSimpleName() + "{", "}")
                .add("username=" + this.user.getUsername())
                .add("albumId=" + this.albumId)
                .add("insertionTime=" + this.insertionTime)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;
        BacklogEntry backlogEntry = (BacklogEntry) other;
        return  Objects.equals(this.user, backlogEntry.user) &&
                Objects.equals(this.albumId, backlogEntry.albumId) &&
                Objects.equals(this.insertionTime, backlogEntry.insertionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, albumId, insertionTime);
    }

    public static class BacklogEntryId implements Serializable {

        private static final long serialVersionUID = 1L;

        private String user;
        private Long albumId;

        public String getUsername() {
            return this.user;
        }

        public BacklogEntryId setUsername(String username) {
            this.user = username;
            return this;
        }

        public Long albumId() {
            return this.albumId;
        }

        public BacklogEntryId setAlbumId(Long albumId) {
            this.albumId = albumId;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", BacklogEntryId.class.getSimpleName() + "{", "}")
                    .add("username=" + this.user)
                    .add("albumId=" + this.albumId)
                    .toString();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || this.getClass() != other.getClass())
                return false;
            BacklogEntryId backlogEntryId = (BacklogEntryId) other;
            return  Objects.equals(this.user, backlogEntryId.user) &&
                    Objects.equals(this.albumId, backlogEntryId.albumId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.user, this.albumId);
        }

    }

}
