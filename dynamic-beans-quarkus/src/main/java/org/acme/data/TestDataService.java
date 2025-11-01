package org.acme.data;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProfile("test")
public class TestDataService implements DataService {
    @Override
    public String fetch() {
        return "TEST data (mock)";
    }
}