package com.example.admin;

import com.example.auth.Action;
import com.example.auth.GroupEntity;
import com.example.auth.PermissionEntity;
import com.example.auth.UserEntity;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class PermissionAdminService {

    public static class PermissionRequest {
        public String resourceType;
        public Action action;
        public Long userId;
        public Long groupId;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "user-permissions")
    public PermissionResult grantPermission(PermissionRequest req) {
        if (req == null || req.resourceType == null || req.action == null)
            throw new BadRequestException("resourceType and action are required");

        // Check for existing permission to prevent duplicates
        PermissionEntity existing = null;
        if (req.userId != null) {
            UserEntity user = UserEntity.findById(req.userId);
            if (user == null)
                throw new NotFoundException("user not found: " + req.userId);

            existing = PermissionEntity.find("resourceType = ?1 and action = ?2 and user.id = ?3",
                    req.resourceType, req.action, req.userId).firstResult();
        } else if (req.groupId != null) {
            GroupEntity group = GroupEntity.findById(req.groupId);
            if (group == null)
                throw new NotFoundException("group not found: " + req.groupId);

            existing = PermissionEntity.find("resourceType = ?1 and action = ?2 and group.id = ?3",
                    req.resourceType, req.action, req.groupId).firstResult();
        } else {
            throw new BadRequestException("Either userId or groupId must be provided.");
        }

        if (existing != null) {
            return new PermissionResult(existing, false); // Existing permission
        }

        // Create new permission
        PermissionEntity p = new PermissionEntity();
        p.resourceType = req.resourceType;
        p.action = req.action;

        if (req.userId != null) {
            p.user = UserEntity.findById(req.userId);
        } else {
            p.group = GroupEntity.findById(req.groupId);
        }

        p.persist();
        return new PermissionResult(p, true); // New permission created
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "user-permissions")
    public boolean revokePermission(Long id) {
        return PermissionEntity.deleteById(id);
    }

    public static class PermissionResult {
        public final PermissionEntity permission;
        public final boolean wasCreated;

        public PermissionResult(PermissionEntity permission, boolean wasCreated) {
            this.permission = permission;
            this.wasCreated = wasCreated;
        }
    }
}
