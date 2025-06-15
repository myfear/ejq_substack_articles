package org.acme;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/colors")
public class ColorPaletteResource {

    @Inject
    ColorExtractorService colorExtractorService;

    @Inject
    ColorNamer colorNamer;

    public static Logger LOG = Logger.getLogger(ColorPaletteResource.class);

    @POST
    @Path("/extract")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractColorPalette(FileUploadInput file) throws IOException {

        // 0. Check if we received a file
        if (file == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error: No file uploaded.\"}").build();

        }

        try {
            // 1. Read image bytes from the uploaded file
            if (file.file == null || file.file.isEmpty()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Error: No file uploaded.\"}").build();
            }
            InputStream inputStream = Files.newInputStream(file.file.get(0).uploadedFile());

            // 2. Extract the hex color codes from the image
            List<String> hexCodes = colorExtractorService.extractColors(inputStream, 6); // Extract 6 colors

            // 3. For each hex code, call the AI service to get a name
            List<NamedColor> namedPalette = hexCodes.parallelStream() // Use parallelStream for efficiency
                    .map(hex -> {
                        String name = colorNamer.nameColor(hex).replace("\"", ""); // Clean up quotes from LLM output
                        return new NamedColor(hex, name);
                    })
                    .collect(Collectors.toList());

            // 4. Return the combined data as JSON
            return Response.ok(namedPalette).build();
        } catch (Exception e) {
            LOG.error("Error processing the image", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Could not process the image.\"}").build();
        }
    }

    public static class FileUploadInput {

        @FormParam("text")
        public String text;

        @FormParam("file")
        public List<FileUpload> file;

    }

    public static class NamedColor {
        public String hex;
        public String name;

        public NamedColor(String hex, String name) {
            this.hex = hex;
            this.name = name;
        }
    }

}
