package storage;

import application.entities.*;
import application.util.AvatarGenerator;
import com.github.javafaker.Faker;
import deezer.client.DeezerClient;
import deezer.model.Album;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

final class DataGenerator {

    private static final int TOP_ALBUMS_NUMBER;

    // Users data properties
    private static final int MIN_USERS;
    private static final int MAX_USERS;

    // Reviews data properties
    private static final int MIN_ALBUM_REVIEWS;
    private static final int MAX_ALBUM_REVIEWS;
    private static final int MIN_ALBUM_REVIEW_RATING;
    private static final int MAX_ALBUM_REVIEW_RATING;
    private static final int MIN_ALBUM_REVIEW_PARAGRAPHS;
    private static final int MAX_ALBUM_REVIEW_PARAGRAPHS;

    private static final int MIN_TOP_ALBUM_REVIEWS;
    private static final int MAX_TOP_ALBUM_REVIEWS;
    private static final int MIN_TOP_ALBUM_REVIEW_RATING;
    private static final int MAX_TOP_ALBUM_REVIEW_RATING;
    private static final int MIN_TOP_ALBUM_REVIEW_PARAGRAPHS;
    private static final int MAX_TOP_ALBUM_REVIEW_PARAGRAPHS;

    // Votes data properties
    private static final int MIN_ALBUM_REVIEW_VOTES;
    private static final int MAX_ALBUM_REVIEW_VOTES;

    private static final int MIN_TOP_ALBUM_REVIEW_VOTES;
    private static final int MAX_TOP_ALBUM_REVIEW_VOTES;

    // Backlog data properties
    private static final int MIN_USER_BACKLOG_SIZE;
    private static final int MAX_USER_BACKLOG_SIZE;

    // Reports data properties
    private static final int MIN_REVIEW_REPORTS;
    private static final int MAX_REVIEW_REPORTS;

    private static ThreadLocalRandom random = ThreadLocalRandom.current();
    private static Faker faker = Faker.instance();
    private static Date currentDate;
    private static Date pastDate;

    private static List<Long> topAlbumsIds;
    private static List<Long> albumsIds;

    static {
        try {
            Properties properties = new Properties();
            properties.load(DataGenerator.class.getClassLoader().getResourceAsStream("generated_data.properties"));

            TOP_ALBUMS_NUMBER = Integer.parseInt(properties.getProperty("topAlbumsNumber"));

            MIN_USERS = Integer.parseInt(properties.getProperty("minUsers"));
            MAX_USERS = Integer.parseInt(properties.getProperty("maxUsers"));

            MIN_ALBUM_REVIEWS = Integer.parseInt(properties.getProperty("minAlbumReviews"));
            MAX_ALBUM_REVIEWS = Integer.parseInt(properties.getProperty("maxAlbumReviews"));
            MIN_ALBUM_REVIEW_RATING = Integer.parseInt(properties.getProperty("minAlbumReviewRating"));
            MAX_ALBUM_REVIEW_RATING = Integer.parseInt(properties.getProperty("maxAlbumReviewRating"));
            MIN_ALBUM_REVIEW_PARAGRAPHS = Integer.parseInt(properties.getProperty("minAlbumReviewParagraphs"));
            MAX_ALBUM_REVIEW_PARAGRAPHS = Integer.parseInt(properties.getProperty("maxAlbumReviewParagraphs"));

            MIN_TOP_ALBUM_REVIEWS = Integer.parseInt(properties.getProperty("minTopAlbumReviews"));
            MAX_TOP_ALBUM_REVIEWS = Integer.parseInt(properties.getProperty("maxTopAlbumReviews"));
            MIN_TOP_ALBUM_REVIEW_RATING = Integer.parseInt(properties.getProperty("minTopAlbumReviewRating"));
            MAX_TOP_ALBUM_REVIEW_RATING = Integer.parseInt(properties.getProperty("maxTopAlbumReviewRating"));
            MIN_TOP_ALBUM_REVIEW_PARAGRAPHS = Integer.parseInt(properties.getProperty("minTopAlbumReviewParagraphs"));
            MAX_TOP_ALBUM_REVIEW_PARAGRAPHS = Integer.parseInt(properties.getProperty("maxTopAlbumReviewParagraphs"));

            MIN_ALBUM_REVIEW_VOTES = Integer.parseInt(properties.getProperty("minAlbumReviewVotes"));
            MAX_ALBUM_REVIEW_VOTES = Integer.parseInt(properties.getProperty("maxAlbumReviewVotes"));

            MIN_TOP_ALBUM_REVIEW_VOTES = Integer.parseInt(properties.getProperty("minTopAlbumReviewVotes"));
            MAX_TOP_ALBUM_REVIEW_VOTES = Integer.parseInt(properties.getProperty("maxTopAlbumReviewVotes"));

            MIN_USER_BACKLOG_SIZE = Integer.parseInt(properties.getProperty("minUserBacklogSize"));
            MAX_USER_BACKLOG_SIZE = Integer.parseInt(properties.getProperty("maxUserBacklogSize"));

            MIN_REVIEW_REPORTS = Integer.parseInt(properties.getProperty("minReviewReports"));
            MAX_REVIEW_REPORTS = Integer.parseInt(properties.getProperty("maxReviewReports"));

            DeezerClient client = new DeezerClient();
            List<Album> topAlbums = client.getTopAlbums(0, TOP_ALBUMS_NUMBER).getData();
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataGenerator() {
        throw new UnsupportedOperationException();
    }

    static List<User> generateUsers() {
        List<User> users = new LinkedList<>();
        final int usersToGen = random.nextInt(MIN_USERS, MAX_USERS + 1);
        for (int i = 0; i < usersToGen; i++) {
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
            String email;
            uniqueEmailLoop:
            while (true) {
                email = faker.internet().emailAddress();
                for (User user : users)
                    if (user.getEmail().equals(email))
                        continue uniqueEmailLoop;
                break;
            }
            String password = faker.internet().password
                    (12, 30, true, false, true);
            Date signUpDate = faker.date().between(pastDate, currentDate);
            User user = new User()
                    .setUsername(username)
                    .setEmail(email)
                    .setPassword(BCrypt.hashpw(password, BCrypt.gensalt()))
                    .setSignUpDate(signUpDate)
                    .setPicture(AvatarGenerator.randomAvatar(username, 600, AvatarGenerator.Format.SVG))
                    .setRole(User.Role.USER);
            users.add(user);
        }
        return users;
    }

    static List<Review> generateReviews(final List<User> reviewers) {
        List<Review> reviews = new LinkedList<>();
        for (Long albumId : albumsIds) {
            final int minReviews, maxReviews, minRating, maxRating, minParagraphs, maxParagraphs;
            final boolean toReview;
            if (topAlbumsIds.stream().anyMatch(topAlbumId -> topAlbumId.equals(albumId))) {
                minReviews = Math.min(MIN_USERS, MIN_TOP_ALBUM_REVIEWS);
                maxReviews = Math.min(MIN_USERS, MAX_TOP_ALBUM_REVIEWS);
                minRating = MIN_TOP_ALBUM_REVIEW_RATING;
                maxRating = MAX_TOP_ALBUM_REVIEW_RATING;
                minParagraphs = MIN_TOP_ALBUM_REVIEW_PARAGRAPHS;
                maxParagraphs = MAX_TOP_ALBUM_REVIEW_PARAGRAPHS;
                toReview = true;
            } else {
                minReviews = Math.min(MIN_USERS, MIN_ALBUM_REVIEWS);
                maxReviews = Math.min(MIN_USERS, MAX_ALBUM_REVIEWS);
                minRating = MIN_ALBUM_REVIEW_RATING;
                maxRating = MAX_ALBUM_REVIEW_RATING;
                minParagraphs = MIN_ALBUM_REVIEW_PARAGRAPHS;
                maxParagraphs = MAX_ALBUM_REVIEW_PARAGRAPHS;
                toReview = random.nextBoolean();
            }
            if (!toReview)
                continue;
            int reviewsNumber = random.nextInt(minReviews, maxReviews + 1);
            Collections.shuffle(reviewers);
            for (int i = 0; i < reviewsNumber; i++) {
                User reviewer = reviewers.get(i);
                final String content = String.join("\n", faker.lorem().paragraphs
                        (random.nextInt(minParagraphs, maxParagraphs + 1)));
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

    static List<Vote> generateVotes(final List<User> voters, final List<Review> reviews) {
        List<Vote> votes = new LinkedList<>();
        for (Review review : reviews) {
            if (!random.nextBoolean())
                continue;
            final int minVotes, maxVotes;
            if (topAlbumsIds.stream().anyMatch(topAlbumId -> topAlbumId.equals(review.getReviewedAlbumId()))) {
                minVotes = Math.min(MIN_USERS - 1, MIN_TOP_ALBUM_REVIEW_VOTES);
                maxVotes = Math.min(MIN_USERS - 1, MAX_TOP_ALBUM_REVIEW_VOTES);
            } else {
                minVotes = Math.min(MIN_USERS - 1, MIN_ALBUM_REVIEW_VOTES);
                maxVotes = Math.min(MIN_USERS - 1, MAX_ALBUM_REVIEW_VOTES);
            }
            int votesNumber = random.nextInt(minVotes, maxVotes + 1);
            Collections.shuffle(voters);
            for (int i = 0; i < votesNumber; i++) {
                User voter = voters.get(i);
                if (voter.getUsername().equals(review.getReviewer().getUsername()))
                    continue;
                votes.add(new Vote()
                        .setVoter(voter)
                        .setReview(review)
                        .setValue(random.nextBoolean())
                );
            }
        }
        return votes;
    }

    static List<BacklogEntry> generateBacklogEntries(final List<User> users) {
        final int minBacklogSize = Math.min(albumsIds.size(), MIN_USER_BACKLOG_SIZE);
        final int maxBacklogSize = Math.min(albumsIds.size(), MAX_USER_BACKLOG_SIZE);
        List<BacklogEntry> backlogEntries = new LinkedList<>();
        for (User user : users) {
            // Generating the listening backlog
            if (random.nextBoolean()) {
                Collections.shuffle(albumsIds);
                for (int i = 0; i < random.nextInt(minBacklogSize, maxBacklogSize + 1); i++)
                    backlogEntries.add(new BacklogEntry()
                            .setUser(user)
                            .setAlbumId(albumsIds.get(i))
                            .setInsertionTime(faker.date().between(pastDate, currentDate)));
            }
        }
        return backlogEntries;
    }

    static List<Report> generateReports(final List<User> reporters, final List<Review> reviews) {
        final int minReviewReports = Math.min(MIN_USERS - 1, MIN_REVIEW_REPORTS);
        final int maxReviewReports = Math.min(MIN_USERS - 1, MAX_REVIEW_REPORTS);
        List<Report> reports = new LinkedList<>();
        for (Review review : reviews) {
            if (random.nextInt(9 + 1) == 0) {
                Collections.shuffle(reporters);
                for (int i = 0; i < random.nextInt(minReviewReports, maxReviewReports + 1); i++) {
                    User reporter = reporters.get(i);
                    if (reporter.getUsername().equals(review.getReviewer().getUsername()))
                        continue;
                    reports.add(new Report()
                            .setReporter(reporter)
                            .setReview(review));
                }
            }
        }
        return reports;
    }

}
