package endpoints.dispatchers.fragments;

import application.entities.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/header"})
public class HeaderFragmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        final Boolean isModerator = sessionUser == null
                ? null
                : sessionUser.getRole() == User.Role.MODERATOR || sessionUser.getRole() == User.Role.ADMINISTRATOR;
        request.setAttribute("isModerator", isModerator);

        request.getRequestDispatcher("/WEB-INF/jsp/fragments/header.jsp").forward(request, response);
    }

}
