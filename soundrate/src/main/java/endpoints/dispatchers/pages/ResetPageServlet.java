package endpoints.dispatchers.pages;

import application.entities.User;
import application.model.UsersAgent;
import io.jsonwebtoken.*;

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

    private JwtParser jwtParser;

    @Inject
    private UsersAgent usersAgent;

    @Override
    public void init() {
        this.jwtParser = Jwts.parser();
        this.jwtParser.setSigningKeyResolver(new SigningKeyResolverAdapter() {
            @Override
            public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
                final String username = claims.getSubject();
                final User user = ResetPageServlet.this.usersAgent.getUser(username);
                return user == null ? null : user.getPassword().getBytes();
            }
        });
    }

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
                this.jwtParser.parse(token);
                request.setAttribute("token", token);
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/reset.jsp").forward(request, response);
    }

}
