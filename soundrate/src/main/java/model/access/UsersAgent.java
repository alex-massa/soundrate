package model.access;

import model.exceptions.ConflictingEmailAddressException;
import model.exceptions.ConflictingUsernameException;
import model.transfer.Album;
import model.transfer.Review;
import model.transfer.User;
import model.transfer.Vote;
import persistence.Users;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class UsersAgent {

    private static List<User> users;
    private static List<Review> reviews;

    static {
        UsersAgent.users = Users.getUsers();
        UsersAgent.reviews = Users.getReviews();
    }

    private UsersAgent() {
        throw new UnsupportedOperationException();
    }

    public static List<User> getUsers() {
        return UsersAgent.users;
    }

    public static List<Review> getReviews() {
        return UsersAgent.reviews;
    }

    public static User getUser(final String username) {
        return UsersAgent.users.parallelStream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public static User getUserByEmail(final String email) {
        return UsersAgent.users.parallelStream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    public static Review getReview(final long reviewedAlbumId, final String reviewerUsername) {
        return UsersAgent.reviews.parallelStream()
                .filter(review -> review.getReviewedAlbumId().equals(reviewedAlbumId) && review.getReviewerUsername().equals(reviewerUsername))
                .findFirst()
                .orElse(null);
    }

    public static List<Review> getUserReviews(final User user) {
        return UsersAgent.reviews.parallelStream()
                .filter(review -> review.getReviewerUsername().equals(user.getUsername()))
                .sorted(Comparator.comparing(Review::getPublicationDate).reversed())
                .collect(Collectors.toList());
    }

    public static int getUserNumberOfReviews(final User user) {
        List<Review> reviews = UsersAgent.getUserReviews(user);
        return reviews == null ? 0 : reviews.size();
    }

    public static Double getUserAverageAssignedScore(final User user) {
        List<Review> reviews = UsersAgent.getUserReviews(user);
        OptionalDouble averageRating = reviews == null
                ? OptionalDouble.empty()
                : reviews.parallelStream()
                .mapToDouble(Review::getRating)
                .average();
        return averageRating.isPresent() ? averageRating.getAsDouble() : null;
    }

    public static List<Album> getAlbumsInUserBacklog(final User user) {
        return user.getBacklog() == null
                ? null
                : user.getBacklog().parallelStream()
                .map(LibraryAgent::getAlbum)
                .collect(Collectors.toList());
    }

    public static int getUserReputation(final User user) {
        List<Review> reviews = UsersAgent.getUserReviews(user);
        return reviews == null ?
                0 : reviews.parallelStream()
                .mapToInt(UsersAgent::getReviewScore)
                .sum();
    }

    public static List<Vote> getReviewUpvotes(final Review review) {
        return review.getVotes() == null ?
                null : review.getVotes().parallelStream()
                .filter(Vote::getVote)
                .collect(Collectors.toList());
    }

    public static List<Vote> getReviewDownvotes(final Review review) {
        return review.getVotes() == null ?
                null : review.getVotes().parallelStream()
                .filter(vote -> !vote.getVote())
                .collect(Collectors.toList());
    }

    public static int getReviewScore(final Review review) {
        List<Vote> upvotes = UsersAgent.getReviewUpvotes(review);
        List<Vote> downvotes = UsersAgent.getReviewDownvotes(review);
        return (upvotes == null ? 0 : upvotes.size()) - (downvotes == null ? 0 : downvotes.size());
    }

    public static List<Review> getTopReviews() {
        return getTopReviews(0, UsersAgent.reviews.size());
    }

    public static List<Review> getTopReviews(final int index, final int limit) {
        if (index < 0 || limit < 0)
            throw new IllegalArgumentException();
        return UsersAgent.reviews.parallelStream()
                .sorted(Comparator.comparing(UsersAgent::getReviewScore).reversed())
                .skip(index)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static Boolean areUserCredentialsValid(final String username, final String password) {
        User user = UsersAgent.getUser(username);
        return user != null && user.getPassword().equals(password);
    }

    public static void registerUser(final User user) throws ConflictingUsernameException, ConflictingEmailAddressException {
        if (UsersAgent.getUser(user.getUsername()) != null)
            throw new ConflictingUsernameException();
        if (UsersAgent.getUserByEmail(user.getEmail()) != null)
            throw new ConflictingEmailAddressException();
        UsersAgent.users.add(user);
    }

    public static Vote getUserReviewVote(final User voter, final Review review) {
        List<Vote> votes = review.getVotes();
        return votes == null
                ? null
                : votes.parallelStream()
                .filter(vote -> vote.getVoterUsername().equals(voter.getUsername()))
                .findFirst()
                .orElse(null);
    }

    public static void voteReview(final User voter, final Review review, final Boolean value) {
        List<Vote> votes = review.getVotes();
        if (votes == null) {
            votes = new LinkedList<>();
            review.setVotes(votes);
        }
        Vote reviewUserVote = votes.parallelStream()
                .filter(vote -> vote.getVoterUsername().equals(voter.getUsername()))
                .findFirst()
                .orElse(null);
        if (reviewUserVote == null) {
            if (value != null)
                votes.add(new Vote()
                        .setVoterUsername(voter.getUsername())
                        .setVote(value)
                        .setReviewedAlbumId(review.getReviewedAlbumId())
                        .setReviewerUsername(review.getReviewerUsername())
                );
        } else {
            if (value == null)
                votes.remove(reviewUserVote);
            else
                reviewUserVote.setVote(value);
        }
    }

    public static boolean isAlbumInUserBacklog(final User user, final Album album) {
        List<Long> backlog = user.getBacklog();
        return backlog != null && backlog.contains(album.getId());
    }

    public static void addAlbumInUserBacklog(final User user, final Album album) {
        List<Long> backlog = user.getBacklog();
        if (backlog == null) {
            backlog = new LinkedList<>();
            user.setBacklog(backlog);
        }
        user.getBacklog().add(album.getId());
    }

    public static void removeAlbumFromUserBacklog(final User user, final Album album) {
        List<Long> backlog = user.getBacklog();
        if (backlog == null || !backlog.contains(album.getId()))
            return;
        user.getBacklog().remove(album.getId());
        if (backlog.isEmpty())
            user.setBacklog(null);
    }

    public static void publishReview(final Review toPublish) {
        UsersAgent.reviews.add(toPublish);
    }

    public static void editReview(final Review toEdit, final String content, final Integer rating) {
        toEdit.setRating(rating).setContent(content);
    }

    public static void deleteReview(final Review review) {
        UsersAgent.reviews.remove(review);
    }

}
