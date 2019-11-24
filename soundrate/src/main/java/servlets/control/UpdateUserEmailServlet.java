package servlets.control;

import application.business.DataAgent;
import application.exceptions.ConflictingEmailAddressException;
import application.exceptions.UserNotFoundException;
import application.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/update-email"})
public class UpdateUserEmailServlet extends HttpServlet {

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String username = request.getParameter("username");
        final String currentPassword = request.getParameter("cpassword");
        final String newEmail = request.getParameter("nemail");
        if (username == null || username.isEmpty() ||
            currentPassword == null || currentPassword.isEmpty() ||
            newEmail == null || newEmail.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(username)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            User user = this.dataAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.invalidCredentials"));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            user.setEmail(newEmail);
            this.dataAgent.updateUser(user);
        } catch (UserNotFoundException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (ConflictingEmailAddressException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.conflictingEmailAddress"));
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

}
