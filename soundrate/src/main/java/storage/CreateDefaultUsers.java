package storage;

import application.model.User;
import util.AvatarGenerator;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.Date;

@Singleton
@Startup
public class CreateDefaultUsers {

    @PersistenceContext(unitName = "soundratePersistenceUnit")
    private EntityManager entityManager;

    @PostConstruct
    private void createDefaultUsers() {
        final User[] defaultUsers = {
                new User()
                        .setUsername("admin")
                        .setEmail("admin@soundrate.com")
                        .setPassword("password123")
                        .setSignUpDate(new Date())
                        .setPicture(AvatarGenerator.randomAvatar("admin", 600, AvatarGenerator.Format.SVG)),
                new User()
                        .setUsername("mod")
                        .setEmail("mod@soundrate.com")
                        .setPassword("password123")
                        .setSignUpDate(new Date())
                        .setPicture(AvatarGenerator.randomAvatar("mod", 600, AvatarGenerator.Format.SVG))
        };

        for (User user : defaultUsers)
            try {
                if (this.entityManager.find(User.class, user.getUsername()) == null)
                    this.entityManager.persist(user);
            } catch (EntityNotFoundException e) {
                this.entityManager.persist(user);
            }
    }

}
