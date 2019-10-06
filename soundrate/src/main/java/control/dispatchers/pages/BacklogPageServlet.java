package control.dispatchers.pages;

import model.access.UsersAgent;
import model.transfer.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/backlog"})
public class BacklogPageServlet extends HttpServlet {

    private static final long serialVersionUID = -1757813666201711059L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("id");
        if (username == null || username.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        User user = UsersAgent.getUser(username);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("user", user);
        request.setAttribute("backlog", UsersAgent.getAlbumsInUserBacklog(user));
        response.setContentType("text/html");
        request.getRequestDispatcher("/WEB-INF/jsp/pages/backlog.jsp").forward(request, response);
    }

}
