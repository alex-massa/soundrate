package storage;

import application.model.Review;
import application.model.User;
import application.model.Vote;
import util.AvatarGenerator;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class DatabaseInitializer {

    @PersistenceContext(unitName = "soundratePersistenceUnit")
    private EntityManager entityManager;

    @PostConstruct
    private void generateData() {
        try {
            List<User> defaultUsers = new LinkedList<>();
            defaultUsers.add(new User()
                    .setUsername("admin")
                    .setEmail("admin@soundrate.com")
                    .setPassword("password123")
                    .setSignUpDate(new Date())
                    .setPicture(AvatarGenerator.randomAvatar("admin", 600, AvatarGenerator.Format.SVG)));
            defaultUsers.add(new User()
                    .setUsername("mod")
                    .setEmail("mod@soundrate.com")
                    .setPassword("password123")
                    .setSignUpDate(new Date())
                    .setPicture(AvatarGenerator.randomAvatar("mod", 600, AvatarGenerator.Format.SVG)));
            defaultUsers.add(new User()
                    .setUsername("user")
                    .setEmail("user@soundrate.com")
                    .setPassword("password123")
                    .setSignUpDate(new Date())
                    .setPicture(AvatarGenerator.randomAvatar("user", 600, AvatarGenerator.Format.SVG)));

            Logger logger = Logger.getLogger(this.getClass().getSimpleName());
            logger.setLevel(Level.ALL);
            final long begin = System.nanoTime();

            logger.info("Generating users...");
            List<User> users = UsersDataGenerator.generateUsers();
            users.addAll(defaultUsers);
            logger.info("Generating reviews...");
            List<Review> reviews = UsersDataGenerator.generateReviews(users);
            logger.info("Generating votes...");
            List<Vote> votes = UsersDataGenerator.generateVotes(users, reviews);

            logger.info(String.format("Persisting %d users...", users.size()));
            for (User user : users)
                entityManager.persist(user);
            logger.info(String.format("Persisting %d reviews...", reviews.size()));
            for (Review review : reviews)
                entityManager.persist(review);
            logger.info(String.format("Persisting %d votes...", votes.size()));
            for (Vote vote : votes)
                entityManager.persist(vote);

            final long end = System.nanoTime();
            logger.info(String.format("Persisted users data. Time elapsed: %d ms.",
                            TimeUnit.MILLISECONDS.convert(end - begin, TimeUnit.NANOSECONDS)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
