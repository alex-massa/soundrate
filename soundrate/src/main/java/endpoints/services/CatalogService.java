package endpoints.services;

import application.entities.BacklogEntry;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.UsersAgent;
import application.model.exceptions.BacklogEntryNotFoundException;
import application.model.exceptions.ConflictingBacklogEntryException;
import deezer.model.Album;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;

@Path("/")
public class CatalogService {

    @Inject
    private UsersAgent usersAgent;
    @Inject
    private CatalogAgent catalogAgent;
    @Inject
    private Validator validator;

    private Jsonb mapper;

    @PostConstruct
    private void init() {
        this.mapper = JsonbBuilder.create();
    }

    @Path("/get-backlog-entry")
    @GET
    public Response getBacklogEntry(@QueryParam("user") @NotBlank final String username,
                                    @QueryParam("album") @NotNull final Long albumId,
                                    @Context final HttpServletRequest request) {
        final User user = this.usersAgent.getUser(username);
        if (user == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Album album = this.catalogAgent.getAlbum(albumId);
        if (album == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.albumNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final BacklogEntry backlogEntry = this.catalogAgent.getBacklogEntry(username, albumId);
        return Response.ok(this.mapper.toJson(backlogEntry)).build();
    }

    @Path("/update-backlog")
    @POST
    public Response updateBacklog(@FormParam("user") @NotBlank final String username,
                                  @FormParam("album") @NotNull final Long albumId,
                                  @Context final HttpServletRequest request) {
        final User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null || !sessionUser.getUsername().equals(username))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        final User user = this.usersAgent.getUser(username);
        if (user == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.userNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Album album = this.catalogAgent.getAlbum(albumId);
        if (album == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.albumNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        BacklogEntry backlogEntry = this.catalogAgent.getBacklogEntry(username, albumId);
        if (backlogEntry == null) {
            backlogEntry = new BacklogEntry()
                    .setUser(user)
                    .setAlbumId(album.getId())
                    .setInsertionTime(new Date());
            final Set<ConstraintViolation<BacklogEntry>> constraintViolations = this.validator.validate(backlogEntry);
            if (!constraintViolations.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).build();
            try {
                this.catalogAgent.createBacklogEntry(backlogEntry);
            } catch (ConflictingBacklogEntryException e) {
                final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                        .getString("error.conflictingBacklogEntry");
                return Response.status(Response.Status.CONFLICT).entity(response).build();
            }
        } else {
            try {
                this.catalogAgent.deleteBacklogEntry(backlogEntry);
            } catch (BacklogEntryNotFoundException e) {
                final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                        .getString("error.backlogEntryNotFound");
                return Response.status(Response.Status.NOT_FOUND).entity(response).build();
            }
        }
        return Response.ok().build();
    }

}
