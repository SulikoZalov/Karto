package org.project.karto.architecture;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.project.karto.util.ClassesUtil.projectClasses;

class InfrastructureAccessRestrictionTest {

    @Test
    void communication_should_only_be_accessed_by_application_service() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage("..infrastructure.communication..")
                .should()
                .onlyBeAccessed()
                .byAnyPackage(
                        "..application.service..",
                        "..infrastructure.communication.."
                );

        rule.check(projectClasses);
    }

    @Test
    void repository_should_only_be_accessed_by_application_service() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage("..infrastructure.repository..")
                .should()
                .onlyBeAccessed()
                .byAnyPackage(
                        "..application.service..",
                        "..infrastructure.repository.."
                );

        rule.check(projectClasses);
    }

    @Test
    void security_should_only_be_accessed_by_application_service() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage("..infrastructure.security..")
                .should()
                .onlyBeAccessed()
                .byAnyPackage(
                        "..application.service..",
                        "..infrastructure.security.."
                );

        rule.check(projectClasses);
    }
}
