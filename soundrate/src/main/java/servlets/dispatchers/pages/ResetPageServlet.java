package servlets.dispatchers.pages;

import application.business.DataAgent;
import application.model.User;
import io.jsonwebtoken.*;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/reset"})
public class ResetPageServlet extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String token = request.getParameter("token");
        if (token == null)
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
