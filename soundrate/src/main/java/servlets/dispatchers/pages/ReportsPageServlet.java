package servlets.dispatchers.pages;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/reports-manager"})
public class ReportsPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Boolean isModerator = (Boolean) request.getSession().getAttribute("isModerator");
        if (isModerator == null || !isModerator)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        request.getRequestDispatcher("/WEB-INF/jsp/pages/reports-manager.jsp").forward(request, response);
    }

}
