package endpoints.services;

import application.entities.User;
import application.model.UsersAgent;
import application.model.exceptions.ConflictingEmailAddressException;
import application.model.exceptions.ConflictingUsernameException;
import application.util.AvatarGenerator;
import org.mindrot.jbcrypt.BCrypt;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;

@Path("/")
@Singleton
@Lock(LockType.READ)
public class AuthenticationService {

    private static final int DEFAULT_AVATAR_SIZE = 600;
    private static final AvatarGenerator.Format DEFAULT_AVATAR_FORMAT = AvatarGenerator.Format.SVG;

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private Validator validator;

    @Path("/log-in")
    @POST
    public Response logIn(@FormParam("username") @NotBlank final String username,
                          @FormParam("password") @NotBlank final String password,
                          @Context final HttpServletRequest request) {
        final HttpSession session = request.getSession();
        final User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User user = this.usersAgent.getUser(username);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.invalidCredentials");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }
        session.setAttribute("user", user);
        return Response.ok().build();
    }

    @Path("/log-out")
    @POST
    public Response logOut(@Context final HttpServletRequest request) {
        final HttpSession session = request.getSession();
        final User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        session.invalidate();
        return Response.ok().build();
    }

    @Path("/sign-up")
    @POST
    public Response signUp(@FormParam("username") @NotNull @Pattern(regexp = User.USERNAME_PATTERN) final String username,
                           @FormParam("email") @NotBlank @Email final String email,
                           @FormParam("password") @NotNull @Pattern(regexp = User.PASSWORD_PATTERN) final String password,
                           @Context final HttpServletRequest request) {
        final HttpSession session = request.getSession();
        final User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User user = new User()
                .setUsername(username)
                .setEmail(email)
                .setPassword(BCrypt.hashpw(password, BCrypt.gensalt()))
                .setSignUpDate(new Date())
                .setPicture(AvatarGenerator.randomAvatar
                        (username, AuthenticationService.DEFAULT_AVATAR_SIZE, AuthenticationService.DEFAULT_AVATAR_FORMAT))
                .setRole(User.Role.USER);
        final Set<ConstraintViolation<User>> constraintViolations = this.validator.validate(user);
        if (!constraintViolations.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).build();
        try {
            this.usersAgent.createUser(user);
        } catch (ConflictingUsernameException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.conflictingUsername");
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        } catch (ConflictingEmailAddressException e) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.conflictingEmailAddress");
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }
        session.setAttribute("user", user);
        return Response.ok().build();
    }

}
