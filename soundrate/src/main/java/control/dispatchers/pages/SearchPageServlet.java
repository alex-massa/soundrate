package control.dispatchers.pages;

import model.access.LibraryAgent;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/search"})
public class SearchPageServlet extends HttpServlet {

    private static final long serialVersionUID = 7466750352140381400L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        String query = request.getParameter("q");
        if (query == null || query.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else {
            request.setAttribute("artists", LibraryAgent.searchArtists(query));
            request.setAttribute("albums", LibraryAgent.searchAlbums(query));
        }
        request.getRequestDispatcher("/WEB-INF/jsp/pages/search.jsp").forward(request, response);
    }

}
