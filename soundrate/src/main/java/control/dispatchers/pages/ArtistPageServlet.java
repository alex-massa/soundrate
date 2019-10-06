package control.dispatchers.pages;

import model.access.LibraryAgent;
import model.transfer.Artist;
import org.apache.commons.lang.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/artist"})
public class ArtistPageServlet extends HttpServlet {

    private static final long serialVersionUID = -9200072939264770426L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        Artist artist;
        long artistId = NumberUtils.toLong(request.getParameter("id"), -1);
        if (artistId == -1)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((artist = LibraryAgent.getArtist(artistId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else
            request.setAttribute("artist", artist);
        request.getRequestDispatcher("/WEB-INF/jsp/pages/artist.jsp").forward(request, response);
    }

}
