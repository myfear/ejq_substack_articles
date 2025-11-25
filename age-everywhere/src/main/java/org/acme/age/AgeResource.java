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

/**
 * REST resource for calculating age information.
 * <p>
 * This resource provides an endpoint to calculate age based on a date of birth
 * and timezone. It returns detailed age information including calendar breakdown
 * and planetary ages.
 * </p>
 *
 * @see AgeService
 * @see AgeResponse
 */
@Path("/age")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AgeResource {
    private final AgeService svc = new AgeService();

    /**
     * Calculates age information based on date of birth and timezone.
     * <p>
     * This endpoint accepts a date of birth in YYYY-MM-DD format and an optional
     * timezone parameter. It returns comprehensive age information including
     * calendar breakdown and planetary ages.
     * </p>
     *
     * @param dob the date of birth in YYYY-MM-DD format (required)
     * @param tz  the timezone identifier (defaults to "Europe/Berlin" if not provided)
     * @return an {@link AgeResponse} containing age information
     * @throws BadRequestException if the date of birth parameter is missing, blank,
     *                            or cannot be parsed
     * @throws jakarta.ws.rs.WebApplicationException if the timezone is invalid
     */
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
