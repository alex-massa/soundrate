package control.dispatchers.pages;

import model.access.LibraryAgent;
import model.access.UsersAgent;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/index", ""})
public class IndexPageServlet extends HttpServlet {

    private static final long serialVersionUID = -9153882973855716121L;

    private static final int ALBUMS_TO_DISPLAY = 8;
    private static final int REVIEWS_TO_DISPLAY = 2;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // @todo remove the following synchronized block
        /*
        synchronized (request.getSession()) {
			request.getSession().setAttribute("username", UsersAgent.getUsers().iterator().next().getUsername());
		}
		*/
        response.setContentType("text/html");
        request.setAttribute("albums", LibraryAgent.getTopAlbums(0, ALBUMS_TO_DISPLAY));
        request.setAttribute("reviews", UsersAgent.getTopReviews(0, REVIEWS_TO_DISPLAY));
        request.getRequestDispatcher("/WEB-INF/jsp/pages/index.jsp").forward(request, response);
    }

}
