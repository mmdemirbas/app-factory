package com.appfactory.application.arch

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.Test

class ApplicationArchitectureTest {

    private val appClasses = ClassFileImporter()
        .importPackages("com.appfactory.application")

    @Test
    fun `application must not depend on infrastructure`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.appfactory.infrastructure..")
            .check(appClasses)
    }

    @Test
    fun `application must not depend on backend`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.appfactory.backend..")
            .check(appClasses)
    }

    @Test
    fun `application must not depend on clients`() {
        noClasses()
            .that().resideInAPackage("com.appfactory.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.appfactory.clients..")
            .check(appClasses)
    }
}
