package servlets.dispatchers.pages;

import application.business.DataAgent;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/top"})
public class TopAlbumsPageServlet extends HttpServlet {

    private static final long serialVersionUID = -5787707038249348047L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("albums", this.dataAgent.getTopAlbums(0, 100));
        request.getRequestDispatcher("/WEB-INF/jsp/pages/top.jsp").forward(request, response);
    }

}
