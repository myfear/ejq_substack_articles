package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/dns")
public class DnsResource {

    @Inject
    DnsAiService dnsAiService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDnsInfoForDomain(@QueryParam("domain") String domain) {
        return dnsAiService.getDnsInfo(domain);
    }
}