package at.doml.restinfo;

import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.junit.Test;
import java.util.Arrays;
import java.util.Objects;
import static at.doml.restinfo.MockUtils.handlerMapping;
import static at.doml.restinfo.MockUtils.requestMapping;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class RestApiInfoGeneratorTest {

    //
    // TESTS
    //
    @Test
    public void restApiInfoGeneratorShouldGenerateCorrectNumberOfControllersAndApiSectionsWhenNoneAreExcluded() {
        final class SomeApiSection {}
        final class SomeOtherApiSection {}

        RestApiInfoGenerator restApiInfoGenerator = restApiInfoGenerator(
                handlerMapping().handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path1"))
                        .handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path2"))
                        .handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path3"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path4"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path5"))
        );

        assertValue("number of api sections", 2, restApiInfoGenerator.getNumberOfApiSections());
        assertValue("number of controllers", 5, restApiInfoGenerator.getNumberOfControllers());
    }

    @Test
    public void restApiInfoGeneratorShouldGenerateCorrectNumberOfControllersAndApiSectionsWhenSomeAreExcluded() {
        final class SomeApiSection {}
        final class SomeOtherApiSection {}
        final class IgnoredApiSection {}

        RestApiInfoGenerator restApiInfoGenerator = restApiInfoGenerator(
                GeneratorSettings.builder().exclude(IgnoredApiSection.class),
                handlerMapping().handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path1"))
                        .handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path2"))
                        .handlerMethod(requestMapping().beanType(IgnoredApiSection.class).path("path3"))
                        .handlerMethod(requestMapping().beanType(IgnoredApiSection.class).path("path4"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path5"))
        );

        assertValue("number of api sections", 2, restApiInfoGenerator.getNumberOfApiSections());
        assertValue("number of controllers", 3, restApiInfoGenerator.getNumberOfControllers());
    }

    @Test
    public void restApiInfoGeneratorShouldGroupControllerInfosIntoCorrectApiSections() {
        final class SomeApiSection {}
        final class SomeOtherApiSection {}

        RestApiInfoGenerator restApiInfoGenerator = restApiInfoGenerator(
                GeneratorSettings.builder().apiSectionNamingStrategy(n -> n),
                handlerMapping().handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path1"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path2"))
                        .handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path3"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path4"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path5"))
        );

        int[] controllerInfoCounts = new int[2];

        restApiInfoGenerator.forEachApiSection(
                n -> {},
                (name, info) -> {
                    if (Objects.equals(name, SomeApiSection.class.getSimpleName())) {
                        controllerInfoCounts[0] += 1;
                        assertPathIsAnyOf(info.getPath(), "/path1", "/path3");
                    } else if (Objects.equals(name, SomeOtherApiSection.class.getSimpleName())) {
                        controllerInfoCounts[1] += 1;
                        assertPathIsAnyOf(info.getPath(), "/path2", "/path4", "/path5");
                    } else {
                        fail("unexpected api section name: " + name);
                    }
                },
                n -> {}
        );

        assertNumberOfControllers(SomeApiSection.class.getSimpleName(), 2, controllerInfoCounts[0]);
        assertNumberOfControllers(SomeOtherApiSection.class.getSimpleName(), 3, controllerInfoCounts[1]);
    }

    @Test
    public void restApiInfoGeneratorShouldNameApiSectionsUsingProvidedNamingStrategy() {
        final class SomeApiSection {}
        final class SomeOtherApiSection {}

        RestApiInfoGenerator restApiInfoGenerator = restApiInfoGenerator(
                GeneratorSettings.builder().apiSectionNamingStrategy(String::toUpperCase),
                handlerMapping().handlerMethod(requestMapping().beanType(SomeApiSection.class).path("path1"))
                        .handlerMethod(requestMapping().beanType(SomeOtherApiSection.class).path("path2"))
        );

        restApiInfoGenerator.forEachApiSection(
                name -> assertNameIsAnyOf(name,
                        SomeApiSection.class.getSimpleName().toUpperCase(),
                        SomeOtherApiSection.class.getSimpleName().toUpperCase()
                ),
                (n, i) -> {},
                n -> {}
        );
    }

    // TODO maybe test if uses correct type tree generator?

    //
    // HELPER METHODS
    //
    private static RestApiInfoGenerator restApiInfoGenerator(MockUtils.RequestMappingHandlerMappingBuilder builder) {
        return new RestApiInfoGenerator(builder.build());
    }

    private static RestApiInfoGenerator restApiInfoGenerator(GeneratorSettings.Builder generatorSettingsBuilder,
                                                             MockUtils.RequestMappingHandlerMappingBuilder builder) {
        return new RestApiInfoGenerator(generatorSettingsBuilder.build(), builder.build());
    }

    @SuppressWarnings("unchecked")
    private static void anyOfStrings(String message, String actual, String... strings) {
        assertThat(message, actual, anyOf(
                (Matcher<String>[]) Arrays.stream(strings)
                        .map(Is::is)
                        .toArray(Matcher[]::new)
        ));
    }

    //
    // ASSERTIONS
    //
    private static void assertValue(String field, int expected, int actual) {
        assertEquals(field + " is incorrect", expected, actual);
    }

    private static void assertNumberOfControllers(String apiSectionName, int expected, int actual) {
        assertEquals("api section '" + apiSectionName + "' has incorrect number of controllers", expected, actual);
    }

    private static void assertPathIsAnyOf(String actual, String... paths) {
        anyOfStrings("unexpected value in path", actual, paths);
    }

    private static void assertNameIsAnyOf(String actual, String... names) {
        anyOfStrings("unexpected name", actual, names);
    }
}
