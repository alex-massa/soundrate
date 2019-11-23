package servlets.control;

import application.business.DataAgent;
import application.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/log-in"})
public class LogInServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        final User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String username = request.getParameter("username");
        final String password = request.getParameter("password");
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty() || !password.matches("^(?=(.*\\d){2})[0-9a-zA-Z]{8,72}$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = this.dataAgent.getUser(username);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.invalidCredentials"));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        session.setAttribute("user", user);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
