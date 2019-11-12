package servlets.control;

import application.business.DataAgent;
import application.exceptions.BacklogEntryNotFoundException;
import application.exceptions.ConflictingBacklogEntryException;
import application.model.BacklogEntry;
import application.model.User;
import deezer.model.Album;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

@WebServlet({"/update-user-backlog"})
public class UpdateUserBacklogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long albumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (albumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = this.dataAgent.getUser(sessionUser.getUsername());
        if (user == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Album album = this.dataAgent.getAlbum(albumId);
        if (album == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        BacklogEntry backlogEntry = this.dataAgent.getBacklogEntry(sessionUser.getUsername(), albumId);
        if (backlogEntry == null) {
            backlogEntry = new BacklogEntry()
                    .setUser(user)
                    .setAlbumId(album.getId())
                    .setInsertionTime(new Date());
            try {
                this.dataAgent.createBacklogEntry(backlogEntry);
            } catch (ConflictingBacklogEntryException e) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.conflictingBacklogEntry"));
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
        }
        else {
            try {
                this.dataAgent.deleteBacklogEntry(backlogEntry);
            } catch (BacklogEntryNotFoundException e) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.backlogEntryNotFound"));
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
