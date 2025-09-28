package com.ibm.txc.museum.shop.api;

import java.util.List;

import com.ibm.txc.museum.shop.domain.ShopItem;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    @GET
    @Path("/artworks/{name}/items")

    public List<ShopItem> byArtwork(@PathParam("name") String artworkName) {
        return ShopItem.list("artworkName = ?1 order by title", artworkName);
    }

    @GET
    @Path("/items/{id}")

    public ShopItem byId(@PathParam("id") Long id) {
        ShopItem i = ShopItem.findById(id);
        if (i == null)
            throw new NotFoundException();
        return i;
    }

}