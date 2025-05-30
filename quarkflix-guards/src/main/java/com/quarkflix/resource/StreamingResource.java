package com.quarkflix.resource;

import com.quarkflix.exception.ContentRestrictionException;
import com.quarkflix.exception.FeatureAccessException;
import com.quarkflix.model.User;
import com.quarkflix.service.StreamingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/stream")
public class StreamingResource {

    @Inject
    StreamingService streamingService;

    @GET
    @Path("/play")
    @Produces(MediaType.TEXT_PLAIN)
    public String play(@QueryParam("user") String userName, @QueryParam("movie") String movieId) {
        User user = new User(userName != null ? userName : "Guest", 25, false);
        return streamingService.playMovie(user, movieId != null ? movieId : "default_movie_id");
    }

    @GET
    @Path("/account")
    @Produces(MediaType.TEXT_PLAIN)
    public String account(@QueryParam("user") String userName) {
        User user = new User(userName != null ? userName : "Guest", 25, false);
        return streamingService.getAccountDetails(user);
    }

    @GET
    @Path("/browse")
    @Produces(MediaType.TEXT_PLAIN)
    public String browse(@QueryParam("user") String userName) {
        User user = new User(userName != null ? userName : "Guest", 25, false);
        return streamingService.browseCatalog(user);
    }

    @GET
    @Path("/maturePlay")
    @Produces(MediaType.TEXT_PLAIN)
    public String maturePlay(@QueryParam("user") String userName, @QueryParam("age") int age,
            @QueryParam("movie") String movieId) {
        User user = new User(userName != null ? userName : "Guest", age, false);
        try {
            return streamingService.streamMatureContent(user, movieId != null ? movieId : "mature_movie_id");
        } catch (ContentRestrictionException e) {
            // In a real JAX-RS app, you'd map this to an appropriate HTTP status (e.g., 403
            // Forbidden)
            // using an ExceptionMapper.
            return "ACCESS DENIED: " + e.getMessage();
        }
    }

    @GET
    @Path("/continueWatching")
    @Produces(MediaType.TEXT_PLAIN)
    public String continueWatching(@QueryParam("user") String userName) {
        User user = new User(userName != null ? userName : "Guest", 30, false);
        return streamingService.getContinueWatchingList(user);
    }

    @GET
    @Path("/premiere")
    @Produces(MediaType.TEXT_PLAIN)
    public String premiere(@QueryParam("user") String userName,
            @QueryParam("age") int age,
            @QueryParam("isVip") boolean isVip,
            @QueryParam("movie") String movieId) {
        User user = new User(userName != null ? userName : "Guest", age, isVip);
        try {
            return streamingService.playPremiereMovie(user, movieId != null ? movieId : "premiere_movie_XYZ");
        } catch (FeatureAccessException | ContentRestrictionException e) {
            return "ACCESS DENIED: " + e.getMessage();
        }
    }
}