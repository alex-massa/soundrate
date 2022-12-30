package storage;

import application.entities.*;
import application.util.AvatarGenerator;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class DatabaseInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    private void initializeDatabase() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
        if (Boolean.parseBoolean(properties.getProperty("populateDatabase")))
            this.generateData();
        if (Boolean.parseBoolean(properties.getProperty("generateDefaultUsers")))
            this.generateDefaultUsers();
    }

    private void generateData() {
        Logger logger = Logger.getLogger(this.getClass().getSimpleName());
        logger.setLevel(Level.ALL);
        final long begin = System.nanoTime();

        logger.info("Generating users...");
        List<User> users = DataGenerator.generateUsers();
        logger.info("Generating reviews...");
        List<Review> reviews = DataGenerator.generateReviews(users);
        logger.info("Generating votes...");
        List<Vote> votes = DataGenerator.generateVotes(users, reviews);
        logger.info("Generating backlog entries...");
        List<BacklogEntry> backlogEntries = DataGenerator.generateBacklogEntries(users);
        logger.info("Generating reviews reports...");
        List<Report> reports = DataGenerator.generateReports(users, reviews);

        logger.info(String.format("Persisting %d users...", users.size()));
        users.forEach(this.entityManager::persist);
        logger.info(String.format("Persisting %d reviews...", reviews.size()));
        reviews.forEach(this.entityManager::persist);
        logger.info(String.format("Persisting %d votes...", votes.size()));
        votes.forEach(this.entityManager::persist);
        logger.info(String.format("Persisting %d backlog entries...", backlogEntries.size()));
        backlogEntries.forEach(this.entityManager::persist);
        logger.info(String.format("Persisting %d reviews reports...", reports.size()));
        reports.forEach(this.entityManager::persist);

        final long end = System.nanoTime();
        logger.info(String.format("Persisted users data. Time elapsed: %d ms.",
                TimeUnit.MILLISECONDS.convert(end - begin, TimeUnit.NANOSECONDS)));
    }

    private void generateDefaultUsers() {
        final List<User> defaultUsers = Arrays.asList(
                new User()
                        .setUsername("admin")
                        .setEmail("admin@soundrate.com")
                        .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setPicture(AvatarGenerator.generateRandomAvatarUrl("admin"))
                        .setRole(User.Role.ADMINISTRATOR),
                new User()
                        .setUsername("mod")
                        .setEmail("mod@soundrate.com")
                        .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setPicture(AvatarGenerator.generateRandomAvatarUrl("mod"))
                        .setRole(User.Role.MODERATOR),
                new User()
                        .setUsername("user")
                        .setEmail("user@soundrate.com")
                        .setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()))
                        .setSignUpDate(new Date())
                        .setPicture(AvatarGenerator.generateRandomAvatarUrl("user"))
                        .setRole(User.Role.USER)
        );

        defaultUsers.forEach(user -> {
            if (this.entityManager.find(User.class, user.getUsername()) == null)
                this.entityManager.persist(user);
        });
    }

}
