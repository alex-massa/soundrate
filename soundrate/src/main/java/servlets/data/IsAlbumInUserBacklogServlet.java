package servlets.data;

import application.entities.BacklogEntry;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.UsersAgent;
import deezer.model.Album;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/is-album-in-user-backlog"})
public class IsAlbumInUserBacklogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private CatalogAgent catalogAgent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String username = request.getParameter("user");
        final long albumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (username == null || username.isEmpty()
                || albumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User user = this.usersAgent.getUser(username);
        if (user == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Album album = this.catalogAgent.getAlbum(albumId);
        if (album == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final BacklogEntry backlogEntry = this.catalogAgent.getBacklogEntry(username, albumId);
        response.getWriter().write(String.valueOf(backlogEntry != null));
    }

}
