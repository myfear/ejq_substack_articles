package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/totp")
public class TotpResource {

    @Inject
    VaultService vaultService;

    @GET
    @Path("/register/{username}")
    @Produces(MediaType.TEXT_HTML)
        public String register(@PathParam("username") String username) {
        
        return vaultService.createTotpKey(username)
                .map(key -> """
                    <!DOCTYPE html>
                    <html>
                        <head><title>Scan QR Code</title></head>
                        <body>
                            <h2>Scan this QR code with Google Authenticator</h2>
                            <img src="data:image/png;base64,%s" alt="TOTP QR Code"/>
                        </body>
                    </html>
                    """.formatted(key.getBarcode()))
                .orElse("Failed to generate TOTP key");
    }
}