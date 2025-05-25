package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.getunleash.Unleash;
import io.quarkiverse.unleash.FeatureToggle;

@ApplicationScoped
public class GreetingService {

    @Inject
    Unleash unleash;

    @FeatureToggle(name = "newAwesomeFeature")
    Instance<Boolean> newAwesomeFeatureEnabled;

    public String getGreeting() {
        if (unleash.isEnabled("newAwesomeFeature")) {
            return "🎉 Hello from the NEW Awesome Feature!";
        }
        return "👋 Hello from the old world.";
    }

    public String getAnnotatedGreetingStatus() {
        return newAwesomeFeatureEnabled.get()
                ? "Annotation says: Feature is ON! ✅"
                : "Annotation says: Feature is OFF. ❌";
    }
}
