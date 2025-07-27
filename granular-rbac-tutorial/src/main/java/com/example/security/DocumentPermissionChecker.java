package com.example.security;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.example.entity.DocumentRight;
import com.example.service.PermissionService;

import io.quarkus.security.PermissionChecker;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DocumentPermissionChecker {

    @Inject
    PermissionService perms;

    @PermissionChecker("document-read")
    public boolean canRead(SecurityIdentity identity, Long id) {
        return check(identity, id, DocumentRight.READ);
    }

    @PermissionChecker("document-write")
    public boolean canWrite(SecurityIdentity identity, Long id) {
        return check(identity, id, DocumentRight.WRITE);
    }

    @PermissionChecker("document-delete")
    public boolean canDelete(SecurityIdentity identity, Long id) {
        return check(identity, id, DocumentRight.DELETE);
    }

    @PermissionChecker("document-share")
    public boolean canShare(SecurityIdentity identity, Long id) {
        return check(identity, id, DocumentRight.SHARE);
    }

    private boolean check(SecurityIdentity identity, Long id, DocumentRight r) {
        if (identity.isAnonymous())
            return false;
        JsonWebToken jwt = (JsonWebToken) identity.getPrincipal();
        String uid = jwt.getClaim("user_id");
        return uid != null && perms.hasPermission(Long.parseLong(uid), id, r);
    }
}