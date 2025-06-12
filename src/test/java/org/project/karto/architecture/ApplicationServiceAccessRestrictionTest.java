package org.project.karto.architecture;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.project.karto.util.ClassesUtil.projectClasses;

class ApplicationServiceAccessRestrictionTest {

    @Test
    void application_service_only_used_in_controller() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage("..application.service..")
                .should()
                .onlyBeAccessed()
                .byAnyPackage(
                        "..application.service..",
                        "..application.controller.."
                );

        rule.check(projectClasses);
    }
}
