package org.acme.i18n;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public class LocalizedMessageRepository implements PanacheRepositoryBase<LocalizedMessage, Long> {

  private static final Logger LOG = Logger.getLogger(LocalizedMessageRepository.class);

    public Optional<String> findMessage(String key, Locale locale) {
        LOG.info("LOCALE: " + locale.toString());
        Optional<LocalizedMessage> message = find("messageKey = ?1 and langTag = ?2", key, locale.toString())
                .firstResultOptional();
        if (message.isPresent())
            return Optional.of(message.get().messageContent);

        if (!locale.getCountry().isEmpty()) {
            message = find("messageKey = ?1 and langTag = ?2", key, locale.getLanguage()).firstResultOptional();
            if (message.isPresent())
                return Optional.of(message.get().messageContent);
        }

        return Optional.empty();
    }
}