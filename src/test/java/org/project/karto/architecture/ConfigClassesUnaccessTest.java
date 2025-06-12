package org.project.karto.architecture;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.project.karto.util.ClassesUtil.projectClasses;

class ConfigClassesUnaccessTest {

    @Test
    void config_classes_should_not_be_accessed_by_other_packages() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage("..infrastructure.config..")
                .should()
                .accessClassesThat()
                .resideInAPackage("..infrastructure.config..");

        rule.check(projectClasses);
    }
}
