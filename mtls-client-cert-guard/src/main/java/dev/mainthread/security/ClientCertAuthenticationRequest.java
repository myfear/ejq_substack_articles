package dev.mainthread.security;

import java.security.cert.X509Certificate;

import io.quarkus.security.identity.request.AuthenticationRequest;

public final class ClientCertAuthenticationRequest implements AuthenticationRequest {

    private final X509Certificate certificate;
    private final java.util.Map<String, Object> attributes = new java.util.concurrent.ConcurrentHashMap<>();

    public ClientCertAuthenticationRequest(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public X509Certificate certificate() {
        return certificate;
    }

    @Override
    public java.util.Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }
}