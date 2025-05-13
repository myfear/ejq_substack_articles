package org.acme;

import io.quarkus.vault.VaultTOTPSecretEngine;
import io.quarkus.vault.secrets.totp.CreateKeyParameters;
import io.quarkus.vault.secrets.totp.KeyDefinition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class VaultService {

    @Inject
    VaultTOTPSecretEngine totpEngine;

    public Optional<KeyDefinition> createTotpKey(String username) {
        CreateKeyParameters params = new CreateKeyParameters("quarkus-demo", username);
        params.setPeriod("1m");
        params.setExported(true); // Ensures the otpauth URL is returned
        return totpEngine.createKey(username, params);
    }

    public boolean validateCode(String username, String code) {
        return totpEngine.validateCode(username, code);
    }
}