package org.project.karto.architecture;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.project.karto.util.ClassesUtil.projectClasses;

class ControllersUnaccessTest {

    @Test
    void controller_should_not_be_accessed_by_anyone() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage("..application.controller..")
                .should()
                .accessClassesThat()
                .resideInAPackage("..application.controller..");

        rule.check(projectClasses);
    }
}
