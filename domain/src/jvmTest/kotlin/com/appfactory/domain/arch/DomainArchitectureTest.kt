package com.appfactory.domain.arch

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.Test

/**
 * Hard gate: domain module must have zero dependencies on infrastructure.
 *
 * This runs as a JVM test task and is enforced in CI.
 * Failure here means an AI agent (or a human) violated the core architecture rule.
 *
 * See docs/ai-workflow.md for the rule explanation.
 */
class DomainArchitectureTest {

    private val domainClasses = ClassFileImporter()
        .importPackages("com.appfactory.domain")

    @Test
    fun `domain must not depend on infrastructure`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.appfactory.infrastructure..")
            .check(domainClasses)
    }

    @Test
    fun `domain must not depend on clients`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.appfactory.clients..")
            .check(domainClasses)
    }

    @Test
    fun `domain must not depend on backend`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.appfactory.backend..")
            .check(domainClasses)
    }

    @Test
    fun `domain common must not import rest of domain`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.domain.common..")
            .should().dependOnClassesThat(
                JavaClass.Predicates.resideInAPackage("com.appfactory.domain..")
                    .and(JavaClass.Predicates.resideOutsideOfPackage("com.appfactory.domain.common.."))
            )
            .check(domainClasses)
    }
}
