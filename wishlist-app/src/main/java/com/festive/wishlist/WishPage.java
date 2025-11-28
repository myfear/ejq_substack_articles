package com.festive.wishlist;

import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ui")
public class WishPage {

    @Inject
    Template wishes; // matches wishes.html

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance show() {
        // Active Record Pattern: use static methods on entity
        List<Wish> active = Wish.findActive();
        List<Wish> archived = Wish.findArchived();
        List<Wishlist> wishlists = Wishlist.findActive();

        return wishes.data("active", active)
                .data("archived", archived)
                .data("wishlists", wishlists);
    }

    // a) New Wishlist
    @POST
    @Path("/wishlist")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response createWishlist(@FormParam("ownerName") String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return Response.seeOther(java.net.URI.create("/ui")).build();
        }
        Wishlist wishlist = new Wishlist();
        wishlist.ownerName = ownerName.trim();
        wishlist.persist();
        return Response.seeOther(java.net.URI.create("/ui")).build();
    }

    // b) New Wish
    @POST
    @Path("/wish")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response createWish(
            @FormParam("description") String description,
            @FormParam("priority") String priority,
            @FormParam("wishlistId") Long wishlistId) {
        if (description == null || description.trim().isEmpty() || wishlistId == null) {
            return Response.seeOther(java.net.URI.create("/ui")).build();
        }
        Wishlist wishlist = Wishlist.findById(wishlistId);
        if (wishlist == null) {
            return Response.seeOther(java.net.URI.create("/ui")).build();
        }
        Wish wish = new Wish();
        wish.description = description.trim();
        wish.priority = priority != null ? priority : "medium";
        wish.kidApproved = false;
        wishlist.addWish(wish);
        wish.persist();
        return Response.seeOther(java.net.URI.create("/ui")).build();
    }

    // c) (Soft) Delete Wish (for Active Wishes)
    @POST
    @Path("/wish/{wishId}/delete")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response softDeleteWish(@PathParam("wishId") Long wishId) {
        Wish wish = Wish.findById(wishId);
        if (wish != null) {
            wish.delete(); // soft delete
        }
        return Response.seeOther(java.net.URI.create("/ui")).build();
    }

    // d) (Soft) Delete WishList (for Active Wishes)
    @POST
    @Path("/wishlist/{wishlistId}/delete")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response softDeleteWishlist(@PathParam("wishlistId") Long wishlistId) {
        Wishlist wishlist = Wishlist.findById(wishlistId);
        if (wishlist != null) {
            wishlist.delete(); // soft delete
        }
        return Response.seeOther(java.net.URI.create("/ui")).build();
    }

    // e) (Hard) Delete Wishes (for Archived Wishes)
    @POST
    @Path("/wish/{wishId}/purge")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response hardDeleteWish(@PathParam("wishId") Long wishId) {
        Wish.hardDelete(wishId);
        return Response.seeOther(java.net.URI.create("/ui")).build();
    }
}