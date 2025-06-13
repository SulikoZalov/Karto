package org.project.karto.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class BoundedContextIsolationTest {

    private static final String DOMAIN_BASE = "org.project.karto.domain";

    @Test
    void user_context_should_not_depend_on_partner_or_giftcard() {
        JavaClasses importedClasses = new ClassFileImporter()
                .importPackages(DOMAIN_BASE + ".user");

        ArchRule rule = noClasses()
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        DOMAIN_BASE + ".companies..",
                        DOMAIN_BASE + ".card.."
                );

        rule.check(importedClasses);
    }

    @Test
    void partner_context_should_not_depend_on_user_or_giftcard() {
        JavaClasses importedClasses = new ClassFileImporter()
                .importPackages(DOMAIN_BASE + ".companies");

        ArchRule rule = noClasses()
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        DOMAIN_BASE + ".user..",
                        DOMAIN_BASE + ".card.."
                );

        rule.check(importedClasses);
    }

    @Test
    void giftcard_context_should_not_depend_on_user_or_partner() {
        JavaClasses importedClasses = new ClassFileImporter()
                .importPackages(DOMAIN_BASE + ".card");

        ArchRule rule = noClasses()
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        DOMAIN_BASE + ".user..",
                        DOMAIN_BASE + ".companies.."
                );

        rule.check(importedClasses);
    }
}
