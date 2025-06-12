package org.project.karto.architecture;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.project.karto.util.ClassesUtil.projectClasses;

class ApplicationUtilAccessRestrictionTest {

    @Test
    void application_util_only_used_in_application_layer() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage("..application..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("..application.util..");

        rule.check(projectClasses);
    }
}
