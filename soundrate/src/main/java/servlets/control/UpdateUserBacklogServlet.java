package servlets.control;

import application.model.DataAgent;
import application.model.exceptions.BacklogEntryNotFoundException;
import application.model.exceptions.ConflictingBacklogEntryException;
import application.entities.BacklogEntry;
import application.entities.User;
import deezer.model.Album;
import org.apache.commons.lang.math.NumberUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;

@WebServlet(urlPatterns = {"/update-user-backlog"})
public class UpdateUserBacklogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private DataAgent dataAgent;

    @Inject
    private Validator validator;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String username = request.getParameter("user");
        final long albumId = NumberUtils.toLong(request.getParameter("album"), Long.MIN_VALUE);
        if (username == null || username.isEmpty() ||
            albumId == Long.MIN_VALUE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(username)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final User user = this.dataAgent.getUser(username);
        if (user == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.userNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final Album album = this.dataAgent.getAlbum(albumId);
        if (album == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.albumNotFound"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        BacklogEntry backlogEntry = this.dataAgent.getBacklogEntry(username, albumId);
        if (backlogEntry == null) {
            backlogEntry = new BacklogEntry()
                    .setUser(user)
                    .setAlbumId(album.getId())
                    .setInsertionTime(new Date());
            Set<ConstraintViolation<BacklogEntry>> constraintViolations = this.validator.validate(backlogEntry);
            if (!constraintViolations.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            try {
                this.dataAgent.createBacklogEntry(backlogEntry);
            } catch (ConflictingBacklogEntryException e) {
                response.getWriter().write
                        (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                                .getString("error.conflictingBacklogEntry"));
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
        } else {
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
