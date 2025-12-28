package dev.mainthread.security;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
@Priority(1000)
public class ClientCertAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger LOG = Logger.getLogger(ClientCertAuthenticationMechanism.class);

    @Inject
    IdentityProviderManager identityProviderManager;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        LOG.infof("authenticate() called for path: %s", context.request().path());
        X509Certificate cert = extractClientCert(context);
        if (cert == null) {
            LOG.warn("No client certificate extracted from request");
            return Uni.createFrom().nullItem();
        }

        LOG.infof("Client certificate found: subject=%s, issuer=%s", 
                cert.getSubjectX500Principal().getName(), 
                cert.getIssuerX500Principal().getName());

        return identityProviderManager.authenticate(new ClientCertAuthenticationRequest(cert))
                .onFailure().recoverWithUni(failure -> {
                    LOG.errorf(failure, "Authentication failed: %s", failure.getMessage());
                    // When authentication fails, return null to trigger challenge
                    return Uni.createFrom().nullItem();
                })
                .onItem().invoke(identity -> {
                    if (identity != null) {
                        LOG.infof("Authentication successful for principal: %s", identity.getPrincipal().getName());
                    } else {
                        LOG.warn("Authentication returned null identity");
                    }
                });
    }

    @Override
    public Uni<Boolean> sendChallenge(RoutingContext context) {
        LOG.infof("sendChallenge() called for path: %s", context.request().path());
        if (context.response().ended()) {
            LOG.warn("Response already ended, cannot send challenge");
            return Uni.createFrom().item(false);
        }
        LOG.warn("Sending 401 challenge response");
        context.response().setStatusCode(401).end("Client certificate rejected.");
        return Uni.createFrom().item(true);
    }

    @Override
    public Uni<io.quarkus.vertx.http.runtime.security.ChallengeData> getChallenge(RoutingContext context) {
        io.quarkus.vertx.http.runtime.security.ChallengeData challengeData = new io.quarkus.vertx.http.runtime.security.ChallengeData(
                401,
                "Client Cert",
                "Client certificate rejected.");
        return Uni.createFrom().item(challengeData);
    }

    private X509Certificate extractClientCert(RoutingContext context) {
        SSLSession session = context.request().sslSession();
        if (session == null) {
            LOG.warn("No SSL session found in request");
            return null;
        }

        LOG.debugf("SSL session found: protocol=%s, cipherSuite=%s", 
                session.getProtocol(), session.getCipherSuite());

        try {
            Certificate[] peer = session.getPeerCertificates();
            if (peer.length == 0) {
                LOG.warn("No peer certificates in SSL session");
                return null;
            }
            LOG.debugf("Found %d peer certificate(s)", peer.length);
            if (peer[0] instanceof X509Certificate x509) {
                return x509;
            }
            LOG.warn("First peer certificate is not an X509Certificate");
            return null;
        } catch (SSLPeerUnverifiedException e) {
            LOG.warnf(e, "SSL peer unverified exception");
            return null;
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error extracting client certificate");
            return null;
        }
    }
}