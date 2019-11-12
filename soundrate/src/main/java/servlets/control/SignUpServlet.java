package servlets.control;

import application.business.DataAgent;
import application.exceptions.ConflictingEmailAddressException;
import application.exceptions.ConflictingUsernameException;
import application.model.User;
import application.util.AvatarGenerator;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

@WebServlet({"/sign-up"})
public class SignUpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_AVATAR_SIZE = 600;
    private static final AvatarGenerator.Format DEFAULT_AVATAR_FORMAT = AvatarGenerator.Format.SVG;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (username == null || username.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        User user = new User()
                .setUsername(username)
                .setEmail(email)
                .setPassword(BCrypt.hashpw(password, BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setPicture(AvatarGenerator.randomAvatar(username, SignUpServlet.DEFAULT_AVATAR_SIZE, SignUpServlet.DEFAULT_AVATAR_FORMAT))
                .setRole(User.Role.USER);
        try {
            this.dataAgent.createUser(user);
        } catch (ConflictingUsernameException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.conflictingUsername"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (ConflictingEmailAddressException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.conflictingEmailAddress"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        request.getSession().setAttribute("user", user);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
