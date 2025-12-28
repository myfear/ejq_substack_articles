package dev.mainthread.security;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Set;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.mainthread.audit.MtlsAuditService;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ClientCertIdentityProvider implements IdentityProvider<ClientCertAuthenticationRequest> {

    private static final Logger LOG = Logger.getLogger(ClientCertIdentityProvider.class);

    @Inject
    CertificateValidator validator;

    @Inject
    MtlsAuditService audit;

    @Override
    public Class<ClientCertAuthenticationRequest> getRequestType() {
        return ClientCertAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(ClientCertAuthenticationRequest request,
            AuthenticationRequestContext context) {
        X509Certificate cert = request.certificate();
        LOG.infof("Validating certificate for subject: %s", cert.getSubjectX500Principal().getName());
        
        CertificateValidator.ValidationResult result = validator.validate(cert);

        if (!result.accepted()) {
            LOG.warnf("Certificate validation failed: %s", result.reason());
            return Uni.createFrom().failure(new SecurityException(result.reason()));
        }

        LOG.infof("Certificate validation successful, fingerprint: %s", result.fingerprint());
        
        // Audit asynchronously on a worker thread to avoid blocking the IO thread
        String fingerprint = result.fingerprint();
        String principalName = cert.getSubjectX500Principal().getName();
        Uni.createFrom().item(() -> {
                    audit.recordAccepted(fingerprint, principalName);
                    return null;
                })
                .runSubscriptionOn(io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool())
                .subscribe().with(
                        v -> LOG.debugf("Audit entry recorded for fingerprint: %s", fingerprint),
                        failure -> LOG.errorf(failure, "Failed to record audit entry: %s", failure.getMessage())
                );

        Principal principal = cert.getSubjectX500Principal();
        SecurityIdentity identity = QuarkusSecurityIdentity.builder()
                .setPrincipal(principal)
                .addRoles(Set.of("mtls-client"))
                .addAttribute("clientCertFingerprint", result.fingerprint())
                .build();

        return Uni.createFrom().item(identity);
    }
}