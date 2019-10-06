package control.agents;

import application.AvatarGenerator;
import model.access.UsersAgent;
import model.exceptions.ConflictingEmailAddressException;
import model.exceptions.ConflictingUsernameException;
import model.transfer.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/sign-user"})
public class SignUserServlet extends HttpServlet {

    private static final long serialVersionUID = 473470265452019146L;

    private static final int DEFAULT_AVATAR_SIZE = 600;
    private static final AvatarGenerator.Format DEFAULT_AVATAR_FORMAT = AvatarGenerator.Format.SVG;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String action = (action = request.getParameter("action")) == null ? "" : action;
        switch (action) {
            case "signup": {
                try {
                    String username = request.getParameter("username");
                    String email = request.getParameter("email");
                    String password = request.getParameter("password");
                    if (username == null || username.isEmpty() ||
                        email == null || email.isEmpty() ||
                        password == null || password.isEmpty())
                    {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                    UsersAgent.registerUser(new User()
                            .setUsername(username)
                            .setEmail(email)
                            .setPassword(password)
                            .setPicture(AvatarGenerator.randomAvatar(username, DEFAULT_AVATAR_SIZE, DEFAULT_AVATAR_FORMAT)));
                    synchronized (session) {
                        session.setAttribute("username", username);
                    }
                } catch (ConflictingUsernameException e) {
                    response.setContentType("text/plain");
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.conflictingUsername"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                } catch (ConflictingEmailAddressException e) {
                    response.setContentType("text/plain");
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.conflictingEmailAddress"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
            }
            break;

            case "signin": {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                if (username == null || username.isEmpty() ||
                    password == null || password.isEmpty())
                {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                if (!UsersAgent.areUserCredentialsValid(username, password)) {
                    response.setContentType("text/plain");
                    response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.invalidCredentials"));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                synchronized (session) {
                    session.setAttribute("username", username);
                }
            }
            break;

            case "signout": {
                synchronized (session) {
                    session.removeAttribute("username");
                }
            }
            break;

            default: {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
