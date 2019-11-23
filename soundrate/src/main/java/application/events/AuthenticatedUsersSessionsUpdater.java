package application.events;

import application.events.qualifiers.UserUpdated;
import application.model.User;

import javax.enterprise.event.Observes;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebListener
public class AuthenticatedUsersSessionsUpdater implements HttpSessionListener, HttpSessionAttributeListener {

    private Map<String, Map<String, HttpSession>> authenticatedUsersSessions = new ConcurrentHashMap<>();

    private void updateUserSession(@Observes @UserUpdated(type = "updated") User user) {
        Map<String, HttpSession> authenticatedUserSessions = this.authenticatedUsersSessions.get(user.getUsername());
        if (authenticatedUserSessions != null)
            for (HttpSession authenticatedUserSession : authenticatedUserSessions.values())
                authenticatedUserSession.setAttribute("user", user);
    }

    private void invalidateUserSession(@Observes @UserUpdated(type = "deleted") User user) {
        Map<String, HttpSession> authenticatedUserSessions = this.authenticatedUsersSessions.get(user.getUsername());
        if (authenticatedUserSessions != null) {
            for (HttpSession authenticatedUserSession : authenticatedUserSessions.values())
                authenticatedUserSession.invalidate();
            this.authenticatedUsersSessions.remove(user.getUsername());
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null)
            return;
        Map<String, HttpSession> authenticatedUserSessions = this.authenticatedUsersSessions.get(user.getUsername());
        if (authenticatedUserSessions != null) {
            authenticatedUserSessions.remove(session.getId());
            if (authenticatedUserSessions.isEmpty())
                this.authenticatedUsersSessions.remove(user.getUsername());
        }
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (!event.getName().equals("user"))
            return;
        User user = (User) event.getValue();
        Map<String, HttpSession> authenticatedUserSessions = this.authenticatedUsersSessions.get(user.getUsername());
        if (authenticatedUserSessions == null) {
            authenticatedUserSessions = new ConcurrentHashMap<>();
            this.authenticatedUsersSessions.put(user.getUsername(), authenticatedUserSessions);
        }
        HttpSession authenticatedUserSession = event.getSession();
        authenticatedUserSessions.put(authenticatedUserSession.getId(), authenticatedUserSession);
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if (!event.getName().equals("user"))
            return;
        User user = (User) event.getValue();
        Map<String, HttpSession> authenticatedUserSessions = this.authenticatedUsersSessions.get(user.getUsername());
        if (authenticatedUserSessions != null) {
            authenticatedUserSessions.remove(event.getSession().getId());
            if (authenticatedUserSessions.isEmpty())
                this.authenticatedUsersSessions.remove(user.getUsername());
        }
    }

}
