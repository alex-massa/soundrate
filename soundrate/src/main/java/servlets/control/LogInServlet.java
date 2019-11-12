package servlets.control;

import application.business.DataAgent;
import application.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/log-in"})
public class LogInServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
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
        request.getSession().setAttribute("user", user);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
