package servlets.control;

import application.entities.User;
import application.model.UsersAgent;
import application.model.exceptions.UserNotFoundException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Set;

@WebServlet(urlPatterns = {"/update-user-role"})
public class UpdateUserRoleServlet extends HttpServlet {

    @Inject
    private UsersAgent usersAgent;

    @Inject
    private Validator validator;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String username = request.getParameter("username");
        final String roleValue = request.getParameter("role");
        if (username == null || username.isEmpty()
                || roleValue == null || roleValue.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User.Role role;
        try {
            role = User.Role.valueOf(roleValue);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || sessionUser.getRole() != User.Role.ADMINISTRATOR) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            final User user = this.usersAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            user.setRole(role);
            final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
            if (!constraintViolations.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            this.usersAgent.updateUser(user);
        } catch (UserNotFoundException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
