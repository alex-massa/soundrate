package servlets.data;

import application.business.DataAgent;
import application.model.User;
import deezer.model.Album;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
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

    @Inject
    private DataAgent dataAgent;

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
        User user = this.dataAgent.getUser(sessionUsername);
        if (user == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                    request.getLocale()).getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Album album = this.dataAgent.getAlbum(albumId);
        if (album == null) {
            response.getWriter().write(ResourceBundle.getBundle("i18n/strings",
                    request.getLocale()).getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        boolean isAlbumInUserBacklog = this.dataAgent.isAlbumInUserBacklog(user, album);
        response.getWriter().write(String.valueOf(isAlbumInUserBacklog));
    }

}
