package com.example.auth;

import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

@ApplicationScoped
public class PermissionService {

    @CacheResult(cacheName = "user-permissions")
    public Set<String> getPermissionsForUser(String username) {
        Log.info("--- DB HIT: permissions for " + username + " ---");

        UserEntity user = UserEntity.find("username", username).firstResult();
        if (user == null)
            return Set.of();

        var groupIds = (user.groups == null) ? Set.<Long>of()
                : user.groups.stream().map(g -> g.id).collect(Collectors.toSet());

        return PermissionEntity.<PermissionEntity>stream(
                "(user.id = ?1 or group.id in ?2)", user.id, groupIds.isEmpty() ? Set.of(-1L) : groupIds)
                .map(p -> p.resourceType + ":" + p.action)
                .collect(Collectors.toSet());
    }
}