package org.acme.data;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProfile("prod")
public class ProdDataService implements DataService {
    @Override
    public String fetch() {
        return "PROD data (external DB)";
    }
}