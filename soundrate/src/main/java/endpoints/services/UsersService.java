package endpoints.services;

import application.entities.User;
import application.model.UsersAgent;
import application.model.exceptions.ConflictingEmailAddressException;
import application.model.exceptions.UserNotFoundException;
import io.jsonwebtoken.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;

@Path("/")
public class UsersService {

    private static final int RECOVER_ACCOUNT_TOKEN_TIME_TO_LIVE = 3 * 60 * 60 * 1000;

    private JwtParser jwtParser;

    @Resource(mappedName = "mail/soundrateMailSession")
    private Session mailSession;

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private Validator validator;

    @PostConstruct
    private void init() {
        this.jwtParser = Jwts.parser();
        this.jwtParser.setSigningKeyResolver(new SigningKeyResolverAdapter() {
            @Override
            public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
                final String username = claims.getSubject();
                final User user = UsersService.this.usersAgent.getUser(username);
                return user == null ? null : user.getPassword().getBytes();
            }
        });
    }

    @Path("/update-user-email")
    @POST
    public Response updateUserEmail(@FormParam("username") @NotBlank final String username,
                                    @FormParam("cpassword") @NotBlank final String currentPassword,
                                    @FormParam("nemail") @NotBlank @Email final String newEmail,
                                    @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(username))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        try {
            final User user = this.usersAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                        .getString("error.invalidCredentials");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }
            user.setEmail(newEmail);
            final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            this.usersAgent.updateUser(user);
        } catch (UserNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        } catch (ConflictingEmailAddressException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.conflictingEmailAddress");
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }
        return Response.ok().build();
    }

    @Path("/update-user-password")
    @POST
    public Response updateUserPassword(@FormParam("username") @NotBlank final String username,
                                       @FormParam("cpassword") @NotBlank final String currentPassword,
                                       @FormParam("npassword") @NotNull @Pattern(regexp = User.PASSWORD_PATTERN)
                                           final String newPassword,
                                       @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(username))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        try {
            final User user = this.usersAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                        .getString("error.invalidCredentials");
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            this.usersAgent.updateUser(user);
        } catch (UserNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        return Response.ok().build();
    }

    @Path("/update-user-role")
    @POST
    public Response updateUserRole(@FormParam("username") @NotBlank final String username,
                                   @FormParam("role") @NotNull final User.Role role,
                                   @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || sessionUser.getRole() != User.Role.ADMINISTRATOR)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        try {
            final User user = this.usersAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            user.setRole(role);
            final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            this.usersAgent.updateUser(user);
        } catch (UserNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        return Response.ok().build();
    }

    @Path("/recover-user-account")
    @POST
    public Response recoverUserAccount(@FormParam("email") @NotBlank @Email final String email,
                                       @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser != null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User user = this.usersAgent.getUserByEmail(email);
        if (user == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.emailNotLinked");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final String token = Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + UsersService.RECOVER_ACCOUNT_TOKEN_TIME_TO_LIVE))
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
        final ResourceBundle emailTemplateBundle = ResourceBundle.getBundle("i18n/templates/email", request.getLocale());
        final MimeMessage message = new MimeMessage(this.mailSession);
        try {
            message.setSubject(emailTemplateBundle.getString("recover.subject"));
            message.setContent(MessageFormat.format(emailTemplateBundle.getString("recover.body"), passwordRecoveryUrl),
                    "text/plain; charset=utf-8");
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(user.getEmail()));
            Transport.send(message);
        } catch (MessagingException e) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    @Path("/reset-user-password")
    @POST
    public Response resetUserPassword(@FormParam("token") final String token,
                                      @FormParam("password") @NotNull @Pattern(regexp = User.PASSWORD_PATTERN)
                                      final String password,
                                      @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser != null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        try {
            final String username = this.jwtParser.parseClaimsJws(token).getBody().getSubject();
            final User user = this.usersAgent.getUser(username);
            if (user == null)
                throw new UserNotFoundException();
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            this.usersAgent.updateUser(user);
        } catch (JwtException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.invalidLink");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        } catch (UserNotFoundException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }
        return Response.ok().build();
    }

}
