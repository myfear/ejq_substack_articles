package org.acme;

import java.util.List;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/geonames")
public class GeoNameResource {

    private static final int PAGE_SIZE = 10;

    /**
     * A full-featured, paginated search endpoint.
     * It searches for names starting with the term, case-insensitively.
     * Results are sorted by population for relevance.
     * Example: /geonames/search?term=berlin&page=0
     */
    @GET
    @Path("/search")
    public List<GeoName> search(
            @QueryParam("term") String term,
            @QueryParam("page") int page) {

        // Use Panache's find() method with sorting and paging.
        return GeoName.find(
                "asciiname ILIKE ?1", // Case-insensitive "starts with" query
                Sort.by("population", Sort.Direction.Descending).and("name"),
                term + "%").page(Page.of(page, PAGE_SIZE)).list();
    }

    /**
     * A lightweight, high-performance autocomplete endpoint.
     * It returns only the names (strings) for a snappy UI experience.
     * Thanks to the GIN index, this remains fast on millions of rows.
     * Example: /geonames/autocomplete?term=mun
     */
    @GET
    @Path("/autocomplete")
    public List<String> autocomplete(@QueryParam("term") String term) {
        // Query for GeoName entities and extract just the names
        return GeoName.find(
                "asciiname ILIKE ?1",
                Sort.by("population", Sort.Direction.Descending).and("name"),
                term + "%")
                .page(Page.of(0, 10))
                .list()
                .stream()
                .map(geoName -> ((GeoName) geoName).name)
                .toList();
    }
}