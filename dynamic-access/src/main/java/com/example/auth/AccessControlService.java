package com.example.auth;

import java.util.Set;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AccessControlService {

    @Inject
    SecurityIdentity identity;

    @Inject
    PermissionService permissionService;

    public boolean canPerformAction(String resourceType, Action action) {
        if (identity.isAnonymous())
            return false;
        if (identity.hasRole("admin"))
            return true;

        String username = identity.getPrincipal().getName();
        Set<String> permissions = permissionService.getPermissionsForUser(username);

        // Check for exact permission first
        if (permissions.contains(resourceType + ":" + action)) {
            return true;
        }

        // Check for permission hierarchy - higher permissions include lower ones
        return hasImpliedPermission(permissions, resourceType, action);
    }

    private boolean hasImpliedPermission(Set<String> permissions, String resourceType, Action requestedAction) {
        // Define permission hierarchy: higher permissions imply lower ones
        switch (requestedAction) {
            case READ:
                // READ is implied by CREATE, UPDATE, DELETE
                return permissions.contains(resourceType + ":" + Action.CREATE) ||
                        permissions.contains(resourceType + ":" + Action.UPDATE) ||
                        permissions.contains(resourceType + ":" + Action.DELETE);
            default:
                return false; // No implied permissions for CREATE, UPDATE, DELETE
        }
    }
}