package servlets.control;

import application.business.DataAgent;
import application.exceptions.UserNotFoundException;
import application.model.User;
import io.jsonwebtoken.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private JwtParser jwtParser;

    @Inject
    private DataAgent dataAgent;

    @Override
    public void init() {
        this.jwtParser = Jwts.parser();
        this.jwtParser.setSigningKeyResolver(new SigningKeyResolverAdapter() {

            @Override
            public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
                String username = claims.getSubject();
                return dataAgent.getUser(username).getPassword().getBytes();
            }

        });
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String token = request.getParameter("token");
        final String password = request.getParameter("password");
        if (token == null || token.isEmpty() ||
            password == null || password.isEmpty() || !password.matches("^(?=(.*\\d){2})[0-9a-zA-Z]{8,72}$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            String username = this.jwtParser.parseClaimsJws(token).getBody().getSubject();
            User user = this.dataAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            this.dataAgent.updateUser(user);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JwtException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.invalidLink"));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (UserNotFoundException e) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

}
