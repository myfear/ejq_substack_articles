package org.acme;

import java.text.Collator;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.quarkus.logging.Log;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/greetings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreetingResource {

    @GET
    public List<Greeting> getAll() {
        return Greeting.listAll();
    }

    @POST
    @Transactional
    public Response add(Greeting greeting) {
        // A seemingly innocent validation check
        if (greeting.name != null && greeting.name.codePointCount(0, greeting.name.length()) > 6) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Name cannot exceed 6 characters\"}")
                    .build();
        }
        greeting.name = Normalizer.normalize(greeting.name, Normalizer.Form.NFC);

        greeting.persist();

        Log.info("Slug: " + createSlugProperly(greeting.name));

        return Response.status(Response.Status.CREATED).entity(greeting).build();
    }

    @GET
    @Path("/search")
    public Response search(@QueryParam("name") String name) {
        if (name == null) {
            return Response.ok(List.of()).build();
        }

        Log.info("Search query received: '" + name + "'");
        Log.info("Search query bytes: " + java.util.Arrays.toString(name.getBytes()));

        String normalizedName = Normalizer.normalize(name, Normalizer.Form.NFC);
        Log.info("Normalized search query: '" + normalizedName + "'");

        // Get all greetings to debug
        List<Greeting> allGreetings = Greeting.listAll();
        Log.info("All greetings in DB:");
        allGreetings.forEach(
                g -> Log.info("  - '" + g.name + "' (bytes: " + java.util.Arrays.toString(g.name.getBytes()) + ")"));

        List<Greeting> results = Greeting.find("name = ?1", normalizedName).list();
        Log.info("Search results count: " + results.size());

        return Response.ok(results).build();
    }

    @GET
    @Path("/sorted")
    public List<Greeting> getSortedByName(@QueryParam("locale") @DefaultValue("en-US") String localeTag) {
        List<Greeting> greetings = Greeting.listAll();
        Locale locale = Locale.forLanguageTag(localeTag);
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY); // Makes it case-insensitive too

        greetings.sort(Comparator.comparing(g -> g.name, collator));
        return greetings;
    }

    @POST
    @Path("/{id}/kiss")
    @Transactional
    public Response addKiss(@PathParam("id") Long id) {
        Greeting greeting = Greeting.findById(id);
        if (greeting == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // U+1F48B is the code point for the kiss mark emoji 💋
        String kissEmoji = new String(Character.toChars(0x1F48B));
        greeting.message = greeting.message + " " + kissEmoji;

        greeting.persist();
        return Response.ok(greeting).build();
    }

    private static String createSlugProperly(String input) {
        StringBuilder slug = new StringBuilder();
        input.codePoints().forEach(codePoint -> {
            if (Character.isLetterOrDigit(codePoint)) {
                slug.append(Character.toLowerCase(Character.toChars(codePoint)[0]));
            }
        });
        return slug.toString();
    }

}