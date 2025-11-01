package org.acme.data;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProfile("dev")
public class DevDataService implements DataService {
    @Override
    public String fetch() {
        return "DEV data (in-memory)";
    }
}