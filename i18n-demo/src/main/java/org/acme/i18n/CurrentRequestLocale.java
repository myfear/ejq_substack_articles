package org.acme.i18n;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Locale;

import org.jboss.logging.Logger;

@RequestScoped
public class CurrentRequestLocale {

private static final Logger LOG = Logger.getLogger(CurrentRequestLocale.class);

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders headers;

    public Locale get() {
        String lang = uriInfo.getQueryParameters().getFirst("lang");
        if (lang != null && !lang.isBlank()) {
            LOG.info("Locale from query parameter: " + lang);
            return Locale.forLanguageTag(lang);
        }

        List<Locale> locales = headers.getAcceptableLanguages();
        if (!locales.isEmpty()) {
            LOG.info("Locale from Accept-Language header: " + locales.get(0));
            return locales.get(0);
        }

        return Locale.ENGLISH;
    }
}
