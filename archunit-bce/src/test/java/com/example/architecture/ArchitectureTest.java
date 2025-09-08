package com.example.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import jakarta.persistence.Entity;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests enforcing BCE (Boundary-Control-Entity) pattern rules.
 * 
 * These tests ensure:
 * - Proper layer separation and dependencies
 * - Consistent naming conventions across layers
 * - No circular dependencies between business components
 * - Correct placement of framework annotations
 */
public class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example");
    }

    @Test
    void bce_layers_are_respected() {
        // BCE Package Boundaries Rule:
        // - Only boundary can talk to control
        // - Control can talk to entity  
        // - Entity has no outgoing deps to boundary or control
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("com.example..")
                .layer("Boundary").definedBy("..orders.boundary..")
                .layer("Control").definedBy("..orders.control..")
                .layer("Entity").definedBy("..orders.entity..")

                // BCE layer access rules:
                .whereLayer("Boundary").mayOnlyAccessLayers("Control", "Entity")
                .whereLayer("Control").mayOnlyAccessLayers("Entity")
                .whereLayer("Entity").mayNotAccessAnyLayer();

        rule.check(classes);
    }

    @Test
    void naming_conventions_match_packages() {
        // Naming Conventions Rule:
        // - ..boundary.. classes end with Resource
        // - ..control.. classes end with Service  
        // - ..entity.. classes end with Entity
        
        classes().that().resideInAnyPackage("..orders.boundary..")
                .and().areNotInnerClasses()
                .and().areNotNestedClasses()
                .should().haveSimpleNameEndingWith("Resource")
                .check(classes);

        classes().that().resideInAnyPackage("..orders.control..")
                .and().areNotInnerClasses()
                .and().areNotNestedClasses()
                .should().haveSimpleNameEndingWith("Service")
                .check(classes);

        classes().that().resideInAnyPackage("..orders.entity..")
                .and().areNotInnerClasses()
                .and().areNotNestedClasses()
                .should().haveSimpleNameEndingWith("Entity")
                .check(classes);
    }

    @Test
    void no_cycles_between_business_components() {
        // No Cycles Rule:
        // - No cycles across slices (e.g., orders vs future components)
        slices().matching("com.example.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @Test
    void jaxrs_annotations_only_in_boundary() {
        // JAX-RS Annotations Rule:
        // - JAX-RS annotations live in boundary only
        classes().that().areAnnotatedWith(Path.class)
                .and().areNotInnerClasses()
                .and().areNotNestedClasses()
                .should().resideInAnyPackage("..orders.boundary..")
                .check(classes);
    }

    @Test
    void entities_are_annotated_and_in_entity_package() {
        // Entity Annotation Rule:
        // - Entities must be annotated with @Entity and live in ..entity..
        classes().that().areAnnotatedWith(Entity.class)
                .and().areNotInnerClasses()
                .and().areNotNestedClasses()
                .should().resideInAnyPackage("..orders.entity..")
                .check(classes);
    }
}