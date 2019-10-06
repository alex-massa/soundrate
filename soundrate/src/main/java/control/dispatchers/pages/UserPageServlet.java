package control.dispatchers.pages;

import model.access.UsersAgent;
import model.transfer.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/user"})
public class UserPageServlet extends HttpServlet {

    private static final long serialVersionUID = 9095734302968795213L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user;
        String username = request.getParameter("id");
        if (username == null || username.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((user = UsersAgent.getUser(username)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else
            request.setAttribute("user", user);
        response.setContentType("text/html");
        request.getRequestDispatcher("/WEB-INF/jsp/pages/user.jsp").forward(request, response);
    }

}
