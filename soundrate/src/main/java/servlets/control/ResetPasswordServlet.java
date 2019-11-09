package servlets.control;

import application.business.DataAgent;
import application.model.User;
import io.jsonwebtoken.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/reset-password"})
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
        String sessionUsername;
        HttpSession session = request.getSession();
        synchronized (session) {
            sessionUsername = session.getAttribute("username") == null
                    ? null
                    : session.getAttribute("username").toString();
        }
        if (sessionUsername != null)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        if (token == null || password == null)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else {
            try {
                String username = this.jwtParser.parseClaimsJws(token).getBody().getSubject();
                User user = this.dataAgent.getUser(username);
                this.dataAgent.updateUserPassword(user, BCrypt.hashpw(password, BCrypt.gensalt()));
            } catch (JwtException e) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.invalidLink"));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

}
