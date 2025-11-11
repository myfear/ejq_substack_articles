package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;

import static org.junit.jupiter.api.condition.JRE.*;

@QuarkusTest
class JreVersionTest {

    @Test
    @EnabledOnJre({ JAVA_17, JAVA_21 })
    void supportedVersions() {
        System.out.println("Runs on Java 17 or 21");
    }

    @Test
    @DisabledForJreRange(min = JAVA_17, max = JAVA_19)
    void skippedOnOldJres() {
        System.out.println("Skipped on 17–19");
    }
}