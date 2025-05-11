package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.qute.Template;
import java.util.Locale;

import org.acme.i18n.AppMessages;
import org.acme.i18n.CurrentRequestLocale;
import org.jboss.logging.Logger;

@Path("/")
public class IndexResource {

private static final Logger LOG = Logger.getLogger(IndexResource.class);

    @Inject
    Template index;

    @Inject
    AppMessages messages;

    @Inject
    CurrentRequestLocale currentRequestLocale;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        Locale locale = new Locale (currentRequestLocale.get().getLanguage());
        LOG.info("Locale from IndexResource: " + locale);
        return index.instance().setLocale(locale.getLanguage()).data("locale", locale).render();
    }
}