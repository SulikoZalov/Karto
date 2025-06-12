package org.project.karto.architecture;

import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.project.karto.util.ClassesUtil.projectClasses;

class ExceptionHandlerUnaccessTest {

    @Test
    void exceptions_should_not_be_accessed_by_any_class() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage("..infrastructure.exceptions_handler..")
                .should()
                .accessClassesThat()
                .resideInAPackage("..infrastructure.exceptions_handler..");

        rule.check(projectClasses);
    }
}
