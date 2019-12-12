package servlets.control;

import application.entities.User;
import application.model.UsersAgent;
import application.model.exceptions.ConflictingEmailAddressException;
import application.model.exceptions.ConflictingUsernameException;
import application.util.AvatarGenerator;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;

@WebServlet(urlPatterns = {"/sign-up"})
public class SignUpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_AVATAR_SIZE = 600;
    private static final AvatarGenerator.Format DEFAULT_AVATAR_FORMAT = AvatarGenerator.Format.SVG;

    @Inject
    private UsersAgent usersAgent;

    @Inject
    private Validator validator;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HttpSession session = request.getSession();
        final User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String username = request.getParameter("username");
        final String email = request.getParameter("email");
        final String password = request.getParameter("password");
        if (username == null || username.isEmpty()
                || email == null || email.isEmpty()
                || password == null || password.isEmpty() || !password.matches("^(?=(.*\\d){2})[0-9a-zA-Z]{8,72}$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User user = new User()
                .setUsername(username)
                .setEmail(email)
                .setPassword(BCrypt.hashpw(password, BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setPicture(AvatarGenerator.randomAvatar
                        (username, SignUpServlet.DEFAULT_AVATAR_SIZE, SignUpServlet.DEFAULT_AVATAR_FORMAT))
                .setRole(User.Role.USER);
        final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
        if (!constraintViolations.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            this.usersAgent.createUser(user);
        } catch (ConflictingUsernameException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.conflictingUsername"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        } catch (ConflictingEmailAddressException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.conflictingEmailAddress"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        session.setAttribute("user", user);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
