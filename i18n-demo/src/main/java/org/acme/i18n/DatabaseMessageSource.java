package org.acme.i18n;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.text.MessageFormat;
import java.util.Locale;

@ApplicationScoped
public class DatabaseMessageSource {

private static final Logger LOG = Logger.getLogger(DatabaseMessageSource.class);

    @Inject
    LocalizedMessageRepository repository;

    @Inject
    CurrentRequestLocale requestLocale;

    @ConfigProperty(name = "quarkus.default-locale", defaultValue = "en")
    String defaultLocaleStr;

    /**
     * Fetches a localized message for the current request's locale.
     */
    public String getMessage(String key, Object... args) {
        return getMessage(key, requestLocale.get(), args);
    }

    /**
     * Fetches a localized message for a specific locale, with fallback to the default.
     */
    public String getMessage(String key, Locale locale, Object... args) {

        LOG.info("Fetching message for key: " + key + ", locale: " + locale + " DefaultLocale: "+  defaultLocaleStr);

        Locale fallbackLocale = Locale.forLanguageTag(defaultLocaleStr);

        String messageContent = repository.findMessage(key, locale)
                .orElseGet(() -> repository.findMessage(key, fallbackLocale)
                .orElse("!!" + key + "!!"));

        return (args != null && args.length > 0)
                ? MessageFormat.format(messageContent, args)
                : messageContent;
    }
}
