package com.example.security;

import java.security.Principal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.SecurityContext;

@RequestScoped
@Named
public class SecurityUtils {

    @Inject
    SecurityContext securityContext;

    public String currentUsername() {
        Principal principal = securityContext.getUserPrincipal();
        return principal != null ? principal.getName() : null;
    }
}