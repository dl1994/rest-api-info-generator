package at.doml.restinfo;

import at.doml.restinfo.type.TypeTreeGenerator;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public final class RestApiInfoSettingsTest {

    //
    // TESTS
    //
    @Test
    public void defaultSectionNamingStrategyShouldSplitWordsAndRemoveControllerSuffix() {
        String controllerName = "ThisIsSomeController";
        RestApiInfoSettings settings = RestApiInfoSettings.builder().build();

        assertEquals(
                "controller name is incorrect",
                "This Is Some",
                settings.apiSectionNamingStrategy.apply(controllerName)
        );
    }

    @Test
    public void restApiInfoSettingsBuilderShouldBuildRestApiInfoSettingsWithCorrectApiSectionNamingStrategy() {
        Function<String, String> strategy = s -> s;
        RestApiInfoSettings settings = RestApiInfoSettings.builder()
                .apiSectionNamingStrategy(strategy)
                .build();

        assertSameObjects(strategy, settings.apiSectionNamingStrategy);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoSettingsBuilderShouldThrowExceptionForNullApiSectionNamingStrategy() {
        RestApiInfoSettings.builder().apiSectionNamingStrategy(null);
    }

    @Test
    public void restApiInfoSettingsBuilderShouldBuildRestApiInfoSettingsWithCorrectTypeTreeGenerator() {
        TypeTreeGenerator generator = new TypeTreeGenerator();
        RestApiInfoSettings settings = RestApiInfoSettings.builder()
                .typeTreeGenerator(generator)
                .build();

        assertSameObjects(generator, settings.typeTreeGenerator);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoSettingsBuilderShouldThrowExceptionForNullTypeTreeGenerator() {
        RestApiInfoSettings.builder().typeTreeGenerator(null);
    }

    @Test
    public void restApiInfoSettingsBuilderShouldBuildRestApiInfoSettingsWithCorrectExcludedControllerClass() {
        Set<Class<?>> excludedClasses = Collections.singleton(Integer.class);
        RestApiInfoSettings settings = RestApiInfoSettings.builder()
                .exclude(Integer.class)
                .build();

        assertSameContents(excludedClasses, settings.excludedControllers);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoSettingsShouldThrowExceptionForExclusionOfNullController() {
        RestApiInfoSettings.builder().exclude(null);
    }

    @Test
    public void restApiInfoSettingsBuilderShouldBuildRestApiInfoSettingsWithCorrectExcludedControllerClasses() {
        Set<Class<?>> excludedClasses = new HashSet<>(Arrays.asList(Integer.class, String.class, Short.class));
        RestApiInfoSettings settings = RestApiInfoSettings.builder()
                .exclude(Integer.class, String.class, Short.class)
                .build();

        assertSameContents(excludedClasses, settings.excludedControllers);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoSettingsShouldThrowExceptionForExclusionOfNullControllers() {
        RestApiInfoSettings.builder().exclude(Void.class, (Class<?>[]) null);
    }

    @Test
    public void multipleExcludeCallsInRestApiInfoSettingsBuilderShouldExcludeAllClassesFromAllCalls() {
        Set<Class<?>> excludedClasses = new HashSet<>(Arrays.asList(Integer.class, String.class, Short.class));
        RestApiInfoSettings settings = RestApiInfoSettings.builder()
                .exclude(Integer.class)
                .exclude(String.class, Short.class)
                .build();

        assertSameContents(excludedClasses, settings.excludedControllers);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoSettingsShouldThrowExceptionForExclusionOfNullControllersElement() {
        RestApiInfoSettings.builder().exclude(Void.class, (Class<?>) null);
    }

    //
    // ASSERTIONS
    //
    private static void assertSameObjects(Object expected, Object actual) {
        assertSame("same object is expected", expected, actual);
    }

    private static void assertSameContents(Set<Class<?>> expected, Set<Class<?>> actual) {
        assertEquals("sets should contain same objects", expected, actual);
    }
}
