package storage;

import application.model.BacklogEntry;
import application.model.Review;
import application.model.User;
import application.model.Vote;
import com.github.javafaker.Faker;
import deezer.client.DeezerClient;
import deezer.model.Album;
import util.AvatarGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

final class UsersDataGenerator {

    private static final int NUMBER_OF_TOP_ALBUMS = 100;

    // Users limits
    private static final int MIN_USERS = 100;
    private static final int MAX_USERS = 200;
    private static final int MIN_BIO_PARAGRAPHS = 1;
    private static final int MAX_BIO_PARAGRAPHS = 3;

    // Reviews limits
    private static final int MIN_REVIEWS = 1;
    private static final int MAX_REVIEWS = 7;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;
    private static final int MIN_PARAGRAPHS = 1;
    private static final int MAX_PARAGRAPHS = 3;

    private static final int MIN_REVIEWS_TOP = 8;
    private static final int MAX_REVIEWS_TOP = 12;
    private static final int MIN_RATING_TOP = 4;
    private static final int MAX_RATING_TOP = 10;
    private static final int MIN_PARAGRAPHS_TOP = 1;
    private static final int MAX_PARAGRAPHS_TOP = 5;

    // Votes limits
    private static final int MIN_VOTES = 0;
    private static final int MAX_VOTES = 12;

    private static final int MIN_VOTES_TOP = 0;
    private static final int MAX_VOTES_TOP = 30;

    // Backlog entries limits
    private static final int MIN_BACKLOG_SIZE = 1;
    private static final int MAX_BACKLOG_SIZE = 30;

    private static ThreadLocalRandom random = ThreadLocalRandom.current();
    private static Faker faker =  Faker.instance();
    private static Date currentDate;
    private static Date pastDate;

    private static List<Long> topAlbumsIds;
    private static List<Long> albumsIds;

    static {
        DeezerClient client = new DeezerClient();

        List<Album> topAlbums = client.getTopAlbums(0, NUMBER_OF_TOP_ALBUMS).getData();
        topAlbumsIds = topAlbums.stream().map(Album::getId).collect(Collectors.toList());
        albumsIds = topAlbumsIds;
        /*
        List<Long> artistsIds = topAlbums.stream()
                .map(album -> album.getArtist().getId()).distinct().collect(Collectors.toList());
        albumsIds = new LinkedList<>();
        for (Long artistId : artistsIds) {
            albumsIds.addAll(client.getArtistAlbums(artistId).getData().stream()
                    .map(Album::getId).collect(Collectors.toList()));
        }
        */

        Calendar calendar = Calendar.getInstance();
        currentDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -1);
        pastDate = calendar.getTime();
    }

    private UsersDataGenerator() {
        throw new UnsupportedOperationException();
    }

    static List<User> generateUsers() {
        List<User> users = new LinkedList<>();
        final int usersToGen = random.nextInt(MIN_USERS, MAX_USERS + 1);
        for (int i = 0; i < usersToGen; i++) {
            // Generating the listening backlog
            List<Long> backlog = null;
            if (random.nextBoolean()) {
                Collections.shuffle(albumsIds);
                backlog = new LinkedList<>();
                for (int j = 0; j < random.nextInt(MIN_BACKLOG_SIZE, MAX_BACKLOG_SIZE + 1); j++)
                    backlog.add(albumsIds.get(j));
            }
            // Generating the user bio
            String bio = null;
            if (random.nextBoolean()) {
                final List<String> paragraphs = faker.lorem().paragraphs
                        (random.nextInt(MIN_BIO_PARAGRAPHS, MAX_BIO_PARAGRAPHS + 1));
                StringBuilder bioBuilder = new StringBuilder();
                for (String paragraph : paragraphs) {
                    bioBuilder.append(paragraph);
                    bioBuilder.append("\n");
                }
                bio = bioBuilder.toString();
            }
            // Generating the user
            String username;
            uniqueUsernameLoop:
            while (true) {
                username = faker.internet().domainWord();
                for (User user : users)
                    if (user.getUsername().equals(username))
                        continue uniqueUsernameLoop;
                break;
            }
            String email = faker.internet().emailAddress();
            String password = faker.internet().password
                    (12, 30, true, false, true);
            Date signUpDate = faker.date().between(pastDate, currentDate);
            User user = new User()
                    .setUsername(username)
                    .setEmail(email)
                    .setPassword(password)
                    .setSignUpDate(signUpDate)
                    .setPicture(AvatarGenerator.randomAvatar(username, 600, AvatarGenerator.Format.SVG))
                    .setBiography(bio);
            users.add(user);
        }
        return users;
    }

    static List<Review> generateReviews(final List<User> users) {
        List<Review> reviews = new LinkedList<>();
        for (Long albumId : albumsIds) {
            final int minReviews, maxReviews, minRating, maxRating, minParagraphs, maxParagraphs;
            final boolean toReview;
            if (topAlbumsIds.stream().anyMatch(topAlbumId -> topAlbumId.equals(albumId))) {
                minReviews = MIN_REVIEWS_TOP;
                maxReviews = MAX_REVIEWS_TOP;
                minRating = MIN_RATING_TOP;
                maxRating = MAX_RATING_TOP;
                minParagraphs = MIN_PARAGRAPHS_TOP;
                maxParagraphs = MAX_PARAGRAPHS_TOP;
                toReview = true;
            } else {
                minReviews = MIN_REVIEWS;
                maxReviews = MAX_REVIEWS;
                minRating = MIN_RATING;
                maxRating = MAX_RATING;
                minParagraphs = MIN_PARAGRAPHS;
                maxParagraphs = MAX_PARAGRAPHS;
                toReview = random.nextBoolean();
            }
            if (!toReview)
                continue;
            int reviewsNumber = random.nextInt(minReviews, maxReviews + 1);
            Collections.shuffle(users);
            for (int i = 0; i < reviewsNumber; i++) {
                User reviewer = users.get(i);
                final List<String> paragraphs = faker.lorem().paragraphs
                        (random.nextInt(minParagraphs, maxParagraphs + 1));
                StringBuilder contentBuilder = new StringBuilder();
                for (String paragraph : paragraphs) {
                    contentBuilder.append(paragraph);
                    contentBuilder.append("\n");
                }
                String content = contentBuilder.toString();
                reviews.add(new Review()
                        .setReviewedAlbumId(albumId)
                        .setReviewer(reviewer)
                        .setContent(content)
                        .setRating(random.nextInt(minRating, maxRating + 1))
                        .setPublicationDate(faker.date().between(reviewer.getSignUpDate(), currentDate))
                );
            }
        }
        return reviews;
    }

    static List<Vote> generateVotes(final List<User> users, final List<Review> reviews) {
        List<Vote> votes = new LinkedList<>();
        for (Review review : reviews) {
            if (!random.nextBoolean())
                continue;
            final int minVotes, maxVotes;
            if (topAlbumsIds.stream().anyMatch(topAlbumId -> topAlbumId.equals(review.getReviewedAlbumId()))) {
                minVotes = MIN_VOTES_TOP;
                maxVotes = MAX_VOTES_TOP;
            } else {
                minVotes = MIN_VOTES;
                maxVotes = MAX_VOTES;
            }
            int votesNumber = random.nextInt(minVotes, maxVotes + 1);
            Collections.shuffle(users);
            for (int j = 0; j < votesNumber; j++) {
                User voter = users.get(j);
                if (voter.getUsername().equals(review.getReviewer().getUsername()))
                    continue;
                votes.add(new Vote()
                        .setVoter(voter)
                        .setReview(review)
                        .setValue(random.nextBoolean() ? +1 : -1)
                );
            }
        }
        return votes;
    }

    static List<BacklogEntry> generateBacklogEntries(final List<User> users) {
        List<BacklogEntry> backlogEntries = new LinkedList<>();
        for (User user : users) {
            // Generating the listening backlog
            if (random.nextBoolean()) {
                Collections.shuffle(albumsIds);
                for (int j = 0; j < random.nextInt(MIN_BACKLOG_SIZE, MAX_BACKLOG_SIZE + 1); j++)
                    backlogEntries.add(new BacklogEntry()
                            .setUser(user)
                            .setAlbumId(albumsIds.get(j))
                            .setInsertionTime(faker.date().between(pastDate, currentDate)));
            }
        }
        return backlogEntries;
    }

}
