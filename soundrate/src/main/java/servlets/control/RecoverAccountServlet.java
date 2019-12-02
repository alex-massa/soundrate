package servlets.control;

import application.model.DataAgent;
import application.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/recover-account"})
public class RecoverAccountServlet extends HttpServlet {

    private static final int TOKEN_TIME_TO_LIVE = 3 * 60 * 60 * 1000;    // 3 hours

    @Resource(mappedName = "mail/soundrateSession")
    private Session smtpSession;

    @Inject
    private DataAgent dataAgent;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String email = request.getParameter("email");
        if (email == null || email.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final User user = dataAgent.getUserByEmail(email);
        if (user == null) {
            response.getWriter().write
                    (ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                            .getString("error.emailNotLinked"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final String token = Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_TIME_TO_LIVE))
                .signWith(SignatureAlgorithm.HS256, user.getPassword().getBytes(StandardCharsets.UTF_8))
                .compact();
        final String passwordRecoveryUrl = request.getScheme() + "://" +
                request.getServerName() +
                ("http".equals(request.getScheme()) && request.getServerPort() == 80
                        || "https".equals(request.getScheme()) && request.getServerPort() == 443
                        ? ""
                        : ":" + request.getServerPort()) +
                request.getRequestURI().substring(0, request.getRequestURI().lastIndexOf('/') + 1) + "reset" +
                "?token=" + token;
        ResourceBundle emailTemplateBundle = ResourceBundle.getBundle("i18n/templates/email", request.getLocale());
        MimeMessage message = new MimeMessage(this.smtpSession);
        try {
            message.setSubject(emailTemplateBundle.getString("recover.subject"));
            message.setContent(MessageFormat.format(emailTemplateBundle.getString("recover.body"), passwordRecoveryUrl),
                    "text/plain; charset=utf-8");
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(user.getEmail()));
            Transport.send(message);
        } catch (MessagingException e) {
            throw new ServletException(e);
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
