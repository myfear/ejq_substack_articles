package dev.mainthread.security;

import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CertificateValidator {

    private static final Logger LOG = Logger.getLogger(CertificateValidator.class);
    private static final String EKU_TLS_CLIENT_AUTH_OID = "1.3.6.1.5.5.7.3.2";

    private final Set<String> allowedFingerprints;
    private final Clock clock = Clock.systemUTC();

    public CertificateValidator(
            @ConfigProperty(name = "mainthread.mtls.allowed-fingerprints") Set<String> allowedFingerprints) {
        this.allowedFingerprints = Objects.requireNonNullElse(allowedFingerprints, Set.of());
    }

    public ValidationResult validate(X509Certificate cert) {
        if (cert == null) {
            LOG.warn("Certificate is null");
            return ValidationResult.rejected("No client certificate presented.");
        }

        Instant now = clock.instant();
        if (now.isBefore(cert.getNotBefore().toInstant()) || now.isAfter(cert.getNotAfter().toInstant())) {
            LOG.warnf("Certificate validity check failed: notBefore=%s, notAfter=%s, now=%s", 
                    cert.getNotBefore(), cert.getNotAfter(), now);
            return ValidationResult.rejected("Client certificate is outside its validity window.");
        }

        if (!ekuAllowsTlsClientAuth(cert)) {
            LOG.warn("Certificate EKU does not allow TLS client authentication");
            return ValidationResult.rejected("Client certificate EKU does not allow TLS client authentication.");
        }

        String fingerprint = sha256Fingerprint(cert);
        LOG.debugf("Certificate fingerprint: %s, allowed fingerprints: %s", fingerprint, allowedFingerprints);
        if (!allowedFingerprints.isEmpty() && !allowedFingerprints.contains(fingerprint)) {
            LOG.warnf("Certificate fingerprint not in allowlist: %s", fingerprint);
            return ValidationResult.rejected("Client certificate fingerprint is not allowlisted: " + fingerprint);
        }

        LOG.infof("Certificate validation passed: fingerprint=%s", fingerprint);
        return ValidationResult.accepted(fingerprint);
    }

    private boolean ekuAllowsTlsClientAuth(X509Certificate cert) {
        try {
            List<String> eku = cert.getExtendedKeyUsage();
            if (eku == null) {
                return true;
            }
            return eku.contains(EKU_TLS_CLIENT_AUTH_OID);
        } catch (Exception e) {
            return false;
        }
    }

    public static String sha256Fingerprint(X509Certificate cert) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(cert.getEncoded());
            return HexFormat.of().withUpperCase().formatHex(digest);
        } catch (CertificateEncodingException cee) {
            throw new IllegalStateException("Unable to encode certificate.", cee);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute certificate fingerprint.", e);
        }
    }

    public record ValidationResult(boolean accepted, String fingerprint, String reason) {
        public static ValidationResult accepted(String fingerprint) {
            return new ValidationResult(true, fingerprint, null);
        }

        public static ValidationResult rejected(String reason) {
            return new ValidationResult(false, null, reason);
        }
    }
}