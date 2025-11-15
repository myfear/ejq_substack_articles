package org.acme.badge;

import io.quarkiverse.quickjs4j.annotations.ScriptImplementation;
import io.roastedroot.quickjs4j.annotations.ScriptInterface;

@ScriptInterface
@ScriptImplementation(location = "badge-generator.js")
public interface BadgeGenerator {
    String createBadge(String label, String value, String theme, String quarkusIconBase64);
}