package control.data;

import model.access.LibraryAgent;
import model.access.UsersAgent;
import model.transfer.Album;
import model.transfer.User;
import org.apache.commons.lang.math.NumberUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet({"/is-album-in-user-backlog"})
public class IsAlbumInUserBacklogServlet extends HttpServlet {

    private static final long serialVersionUID = -3830423929619851024L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionUsername;
        synchronized (session) {
            sessionUsername = session.getAttribute("username") == null ? null : session.getAttribute("username").toString();
        }
        if (sessionUsername == null || sessionUsername.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long albumId = NumberUtils.toLong(request.getParameter("album"), -1);
        if (albumId == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        User user = UsersAgent.getUser(sessionUsername);
        if (user == null) {
            response.setContentType("text/plain");
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Album album = LibraryAgent.getAlbum(albumId);
        if (album == null) {
            response.setContentType("text/plain");
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings", request.getLocale()).getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        boolean isAlbumInUserBacklog = UsersAgent.isAlbumInUserBacklog(user, album);
        response.setContentType("text/plain");
        response.getWriter().write(String.valueOf(isAlbumInUserBacklog));
    }

}
