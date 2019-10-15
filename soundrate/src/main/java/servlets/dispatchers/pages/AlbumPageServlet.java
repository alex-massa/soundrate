package servlets.dispatchers.pages;

import application.business.DataAgent;
import deezer.model.Album;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/album"})
public class AlbumPageServlet extends HttpServlet {

    private static final long serialVersionUID = -8005646432043137746L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Album album;
        long albumId = NumberUtils.toLong(request.getParameter("id"), -1);
        if (albumId == -1)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if ((album = this.dataAgent.getAlbum(albumId)) == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else
            request.setAttribute("album", album);
        request.getRequestDispatcher("/WEB-INF/jsp/pages/album.jsp").forward(request, response);
    }

}
