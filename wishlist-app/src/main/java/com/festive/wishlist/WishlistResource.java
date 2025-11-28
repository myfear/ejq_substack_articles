package com.festive.wishlist;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/wishlist")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WishlistResource {

    // Create wishlist
    @POST
    @Transactional
    public Wishlist create(Wishlist wishlist) {
        if (wishlist == null) {
            throw new jakarta.ws.rs.BadRequestException("Wishlist data is required");
        }
        wishlist.persist();
        return wishlist;
    }

    // Add wish
    @POST
    @Path("/{id}/wish")
    @Transactional
    public Wishlist addWish(@PathParam("id") Long id, Wish wish) {
        if (wish == null) {
            throw new jakarta.ws.rs.BadRequestException("Wish data is required");
        }

        Wishlist list = Wishlist.findById(id);
        if (list == null) {
            throw new jakarta.ws.rs.NotFoundException("Wishlist with id " + id + " not found");
        }
        list.addWish(wish);
        wish.persist();
        return Wishlist.findById(id);
    }

    // Get only active wishlist entries
    @GET
    public List<Wishlist> activeWishlists() {
        return Wishlist.findActive();
    }

    // Soft-delete an entire wishlist
    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        Wishlist list = Wishlist.findById(id);
        if (list != null) {
            list.delete(); // soft delete
        }
    }

    // Soft-delete a wish
    @DELETE
    @Path("/{wishlistId}/wish/{wishId}")
    @Transactional
    public void deleteWish(@PathParam("wishlistId") Long wishlistId,
            @PathParam("wishId") Long wishId) {
        Wish wish = Wish.findById(wishId);
        if (wish == null) {
            throw new jakarta.ws.rs.NotFoundException("Wish with id " + wishId + " not found");
        }
        wish.delete();
    }

    // Restore a soft-deleted wish
    @PUT
    @Path("/wish/{wishId}/restore")
    @Transactional
    public void restoreWish(@PathParam("wishId") Long wishId) {
        Wish.restore(wishId);
    }

    // Hard-delete (North Pole Purge Mode)
    @DELETE
    @Path("/wish/{wishId}/purge")
    @Transactional
    public void hardDeleteWish(@PathParam("wishId") Long wishId) {
        Wish.hardDelete(wishId);
    }
}