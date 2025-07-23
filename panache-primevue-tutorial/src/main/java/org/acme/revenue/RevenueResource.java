package org.acme.revenue;

import java.util.List;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/revenue")
@Produces(MediaType.APPLICATION_JSON)
public class RevenueResource {

    @GET
    public List<MonthlyRevenue> getAll() {
        // Use Panache's listAll method to fetch all records, sorted by period
        return MonthlyRevenue.listAll(Sort.by("period"));
    }
}