package org.acme.age;

import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/age")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AgeResource {
    private final AgeService svc = new AgeService();

    @GET
    public AgeResponse get(@QueryParam("dob") String dob,
            @QueryParam("tz") @DefaultValue("Europe/Berlin") String tz) {
        if (dob == null || dob.isBlank()) {
            throw new BadRequestException("Query param 'dob' is required (YYYY-MM-DD)");
        }
        LocalDate date = LocalDate.parse(dob);
        ZoneId zone = ZoneId.of(tz);
        return svc.compute(date, zone);
    }
}
