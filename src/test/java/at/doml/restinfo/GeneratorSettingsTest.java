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

public final class GeneratorSettingsTest {

    //
    // TESTS
    //
    @Test
    public void defaultSectionNamingStrategyShouldSplitWordsAndRemoveControllerSuffix() {
        String controllerName = "ThisIsSomeController";
        GeneratorSettings settings = GeneratorSettings.builder().build();

        assertEquals(
                "controller name is incorrect",
                "This Is Some",
                settings.apiSectionNamingStrategy.apply(controllerName)
        );
    }

    @Test
    public void generatorSettingsBuilderShouldBuildGeneratorSettingsWithCorrectApiSectionNamingStrategy() {
        Function<String, String> strategy = s -> s;
        GeneratorSettings settings = GeneratorSettings.builder()
                .apiSectionNamingStrategy(strategy)
                .build();

        assertSameObjects(strategy, settings.apiSectionNamingStrategy);
    }

    @Test(expected = NullPointerException.class)
    public void generatorSettingsBuilderShouldThrowExceptionForNullApiSectionNamingStrategy() {
        GeneratorSettings.builder().apiSectionNamingStrategy(null);
    }

    @Test
    public void generatorSettingsBuilderShouldBuildGeneratorSettingsWithCorrectTypeTreeGenerator() {
        TypeTreeGenerator generator = new TypeTreeGenerator();
        GeneratorSettings settings = GeneratorSettings.builder()
                .typeTreeGenerator(generator)
                .build();

        assertSameObjects(generator, settings.typeTreeGenerator);
    }

    @Test(expected = NullPointerException.class)
    public void generatorSettingsBuilderShouldThrowExceptionForNullTypeTreeGenerator() {
        GeneratorSettings.builder().typeTreeGenerator(null);
    }

    @Test
    public void generatorSettingsBuilderShouldBuildGeneratorSettingsWithCorrectExcludedControllerClass() {
        Set<Class<?>> excludedClasses = Collections.singleton(Integer.class);
        GeneratorSettings settings = GeneratorSettings.builder()
                .exclude(Integer.class)
                .build();

        assertSameContents(excludedClasses, settings.excludedControllers);
    }

    @Test(expected = NullPointerException.class)
    public void generatorSettingsShouldThrowExceptionForExclusionOfNullController() {
        GeneratorSettings.builder().exclude(null);
    }

    @Test
    public void generatorSettingsBuilderShouldBuildGeneratorSettingsWithCorrectExcludedControllerClasses() {
        Set<Class<?>> excludedClasses = new HashSet<>(Arrays.asList(Integer.class, String.class, Short.class));
        GeneratorSettings settings = GeneratorSettings.builder()
                .exclude(Integer.class, String.class, Short.class)
                .build();

        assertSameContents(excludedClasses, settings.excludedControllers);
    }

    @Test(expected = NullPointerException.class)
    public void generatorSettingsShouldThrowExceptionForExclusionOfNullControllers() {
        GeneratorSettings.builder().exclude(Void.class, (Class<?>[]) null);
    }

    @Test
    public void multipleExcludeCallsInGeneratorSettingsBuilderShouldExcludeAllClassesFromAllCalls() {
        Set<Class<?>> excludedClasses = new HashSet<>(Arrays.asList(Integer.class, String.class, Short.class));
        GeneratorSettings settings = GeneratorSettings.builder()
                .exclude(Integer.class)
                .exclude(String.class, Short.class)
                .build();

        assertSameContents(excludedClasses, settings.excludedControllers);
    }

    @Test(expected = NullPointerException.class)
    public void generatorSettingsShouldThrowExceptionForExclusionOfNullControllersElement() {
        GeneratorSettings.builder().exclude(Void.class, (Class<?>) null);
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
