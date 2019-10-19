package storage;

import application.model.BacklogEntry;
import application.model.Review;
import application.model.User;
import application.model.Vote;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class DatabaseInitializer {

    @PersistenceContext(unitName = "soundratePersistenceUnit")
    private EntityManager entityManager;

    @PostConstruct
    private void initializeDatabase() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
        if (Boolean.parseBoolean(properties.getProperty("generateDataOnStartup")))
            this.generateData();
    }

    private void generateData() {
        Logger logger = Logger.getLogger(this.getClass().getSimpleName());
        logger.setLevel(Level.ALL);
        final long begin = System.nanoTime();

        logger.info("Generating users...");
        List<User> users = UsersDataGenerator.generateUsers();
        logger.info("Generating reviews...");
        List<Review> reviews = UsersDataGenerator.generateReviews(users);
        logger.info("Generating votes...");
        List<Vote> votes = UsersDataGenerator.generateVotes(users, reviews);
        logger.info("Generating backlog entries...");
        List<BacklogEntry> backlogEntries = UsersDataGenerator.generateBacklogEntries(users);

        logger.info(String.format("Persisting %d users...", users.size()));
        for (User user : users)
            entityManager.persist(user);
        logger.info(String.format("Persisting %d reviews...", reviews.size()));
        for (Review review : reviews)
            entityManager.persist(review);
        logger.info(String.format("Persisting %d votes...", votes.size()));
        for (Vote vote : votes)
            entityManager.persist(vote);
        logger.info(String.format("Persisting %d backlog entries", backlogEntries.size()));
        for (BacklogEntry backlogEntry : backlogEntries)
            entityManager.persist(backlogEntry);

        final long end = System.nanoTime();
        logger.info(String.format("Persisted users data. Time elapsed: %d ms.",
                TimeUnit.MILLISECONDS.convert(end - begin, TimeUnit.NANOSECONDS)));
    }

}
