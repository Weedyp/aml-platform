package com.regtech.gateway;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.regtech")
public class ArchitectureSanityTest {

    @ArchTest
    static final ArchRule internalModulesShouldNotDependOnGateway =
            noClasses()
                    // Target the internal business modules
                    .that().resideInAnyPackage("com.regtech.core..", "com.regtech.kyc..", "com.regtech.batch..")
                    // Enforce that they NEVER touch the Gateway (Web) layer
                    .should().dependOnClassesThat().resideInAPackage("com.regtech.gateway..")
                    .allowEmptyShould(true);
}
