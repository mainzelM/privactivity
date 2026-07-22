package net.privactivity.store.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;



import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = {"net.privactivity.store"},
        importOptions = ImportOption.DoNotIncludeTests.class)
class UseCaseArchitectureTest {

    @ArchTest
    static final ArchRule use_cases_should_only_be_accessed_by_services_or_other_use_cases =
            classes().that()
                     .haveSimpleNameEndingWith("UseCase")
                     .should()
                     .onlyBeAccessed()
                     .byClassesThat()
                     .resideInAnyPackage("net.privactivity.store.service..", "..usecase..");
}
