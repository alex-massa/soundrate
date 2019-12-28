package endpoints.services;

import application.entities.BacklogEntry;
import application.entities.Review;
import application.entities.User;
import application.model.CatalogAgent;
import application.model.UsersAgent;
import application.model.exceptions.BacklogEntryNotFoundException;
import application.model.exceptions.ConflictingBacklogEntryException;
import deezer.model.Album;
import deezer.model.Artist;
import deezer.model.Genre;
import deezer.model.data.Albums;
import deezer.model.data.Artists;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

@Path("/")
@Singleton
@Lock(LockType.READ)
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
                                    @QueryParam("album") @NotNull final Long albumId) {
        final BacklogEntry backlogEntry = this.catalogAgent.getBacklogEntry(username, albumId);
        return Response.ok(this.mapper.toJson(backlogEntry), MediaType.APPLICATION_JSON).build();
    }

    @Path("/get-album")
    @GET
    public Response getAlbum(@QueryParam("id") @NotNull final Long albumId) {
        final Album album = this.catalogAgent.getAlbum(albumId);
        return Response.ok(this.mapper.toJson(album), MediaType.APPLICATION_JSON).build();
    }

    @Path("/get-artist")
    @GET
    public Response getArtist(@QueryParam("id") @NotNull final Long artistId) {
        final Artist artist = this.catalogAgent.getArtist(artistId);
        return Response.ok(this.mapper.toJson(artist), MediaType.APPLICATION_JSON).build();
    }

    @Path("/get-genre")
    @GET
    public Response getGenre(@QueryParam("id") @NotNull final Long genreId) {
        final Genre genre = this.catalogAgent.getGenre(genreId);
        return Response.ok(this.mapper.toJson(genre), MediaType.APPLICATION_JSON).build();
    }

    @Path("/get-album-reviews")
    @GET
    public Response getAlbumReviews(@QueryParam("id") @NotNull final Long albumId,
                                    @QueryParam("index") @Min(0) final Integer index,
                                    @QueryParam("limit") @Min(1) final Integer limit,
                                    @Context final HttpServletRequest request) {
        final Album album = this.catalogAgent.getAlbum(albumId);
        if (album == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.albumNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final List<Review> albumReviews = this.catalogAgent
                .getAlbumReviews(album, index == null ? 0 : index, limit == null ? Integer.MAX_VALUE : limit);
        return Response.ok(this.mapper.toJson(albumReviews), MediaType.APPLICATION_JSON).build();
    }

    @Path("/get-artist-albums")
    @GET
    public Response getArtistAlbums(@QueryParam("id") @NotNull final Long artistId,
                                    @QueryParam("index") @Min(0) final Integer index,
                                    @QueryParam("limit") @Min(1) final Integer limit,
                                    @Context final HttpServletRequest request) {
        final Artist artist = this.catalogAgent.getArtist(artistId);
        if (artist == null) {
            final String response = ResourceBundle.getBundle("i18n/strings/strings", request.getLocale())
                    .getString("error.albumNotFound");
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }
        final Albums artistAlbums = this.catalogAgent
                .getArtistAlbums(artist, index == null ? 0 : index, limit == null ? Integer.MAX_VALUE : limit);
        return Response.ok(this.mapper.toJson(artistAlbums), MediaType.APPLICATION_JSON).build();
    }

    @Path("/get-top-albums")
    @GET
    public Response getTopAlbums(@QueryParam("index") @Min(0) final Integer index,
                                 @QueryParam("limit") @Min(1) final Integer limit) {
        final Albums topAlbums = this.catalogAgent.getTopAlbums
                (index == null ? 0 : index, limit == null ? Integer.MAX_VALUE : limit);
        return Response.ok(this.mapper.toJson(topAlbums == null ? null : topAlbums.getData()), MediaType.APPLICATION_JSON).build();
    }

    @Path("/search-albums")
    @GET
    public Response searchAlbums(@QueryParam("q") @NotBlank final String query,
                                 @QueryParam("index") @Min(0) final Integer index,
                                 @QueryParam("limit") @Min(1) final Integer limit) {
        final Albums albums = this.catalogAgent.searchAlbums
                (query, index == null ? 0 : index, limit == null ? Integer.MAX_VALUE : limit);
        return Response.ok(this.mapper.toJson(albums == null ? null : albums.getData()), MediaType.APPLICATION_JSON).build();
    }

    @Path("/search-artists")
    @GET
    public Response searchArtists(@QueryParam("q") @NotBlank final String query,
                                  @QueryParam("index") @Min(0) final Integer index,
                                  @QueryParam("limit") @Min(1) final Integer limit) {
        final Artists artists = this.catalogAgent.searchArtists
                (query, index == null ? 0 : index, limit == null ? Integer.MAX_VALUE : limit);
        return Response.ok(this.mapper.toJson(artists == null ? null : artists.getData()), MediaType.APPLICATION_JSON).build();
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
