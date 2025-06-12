package org.project.karto.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class DomainSeparationTest {

    private static final String DOMAIN_PACKAGE = "org.project.karto.domain";

    @Test
    void domain_should_only_depend_on_itself_and_java() {
        JavaClasses domainClasses = new ClassFileImporter()
                .importPackages(DOMAIN_PACKAGE);

        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN_PACKAGE + "..")
                .should().dependOnClassesThat()
                .resideOutsideOfPackages(
                        DOMAIN_PACKAGE + "..",
                        "java.."
                );

        rule.check(domainClasses);
    }
}
