package endpoints.dispatchers.pages;

import application.entities.User;
import application.model.UsersAgent;
import application.model.exceptions.UserNotFoundException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/reset"})
public class ResetPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsersAgent usersAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        final String token = request.getParameter("token");
        if (sessionUser != null)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        else if (token == null || token.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else {
            try {
                final String username = JWT.decode(token).getSubject();
                final User user = username == null || username.isEmpty() ? null : this.usersAgent.getUser(username);
                if (user == null)
                    throw new UserNotFoundException();
                JWT.require(Algorithm.HMAC256(user.getPassword())).build().verify(token);
                request.setAttribute("token", token);
            } catch (UserNotFoundException | JWTVerificationException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/reset.jsp").forward(request, response);
    }

}
