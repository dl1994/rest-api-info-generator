package at.doml.restinfo.writer;

import org.junit.Test;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiFunction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public final class RestApiInfoHtmlWriterSettingsTest {

    //
    // TESTS
    //
    @Test
    public void restApiInfoHtmlWriterSettingsBuilderShouldSetCorrectIndentSpacing() {
        RestApiInfoHtmlWriterSettings settings = RestApiInfoHtmlWriterSettings.builder()
                .indentSpacing(10)
                .build();

        assertEquals("same values are expected", 10, settings.indentSpacing);
    }

    @Test(expected = IllegalArgumentException.class)
    public void restApiInfoHtmlWriterSettingsBuilderShouldThrowExceptionForNegativeIndentSpacing() {
        RestApiInfoHtmlWriterSettings.builder().indentSpacing(-1);
    }

    @Test
    public void restApiInfoHtmlWriterSettingsBuilderShouldSetCorrectSectionsToPrint() {
        Set<RestApiInfoHtmlWriterSettings.PrintSection> sectionsToPrint = EnumSet.of(
                RestApiInfoHtmlWriterSettings.PrintSection.MODEL_ATTRIBUTES,
                RestApiInfoHtmlWriterSettings.PrintSection.REQUEST_BODY
        );
        RestApiInfoHtmlWriterSettings settings = RestApiInfoHtmlWriterSettings.builder()
                .printSection(RestApiInfoHtmlWriterSettings.PrintSection.MODEL_ATTRIBUTES)
                .printSection(RestApiInfoHtmlWriterSettings.PrintSection.REQUEST_BODY)
                .build();

        assertEquals("sets should contain same objects", sectionsToPrint, settings.sectionsToPrint);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoHtmlWriterSettingsBuilderShouldThrowExceptionForNullSectionToPrint() {
        RestApiInfoHtmlWriterSettings.builder().printSection(null);
    }

    @Test
    public void restApiInfoHtmlWriterSettingsBuilderShouldSetCorrectStylesheetProvider() {
        StylesheetProvider stylesheetProvider = ignored -> {};
        RestApiInfoHtmlWriterSettings settings = RestApiInfoHtmlWriterSettings.builder()
                .stylesheetProvider(stylesheetProvider)
                .build();

        assertSameObjects(stylesheetProvider, settings.stylesheetProvider);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoHtmlWriterSettingsBuilderShouldThrowExceptionForNullStylesheetProvider() {
        RestApiInfoHtmlWriterSettings.builder().stylesheetProvider(null);
    }

    @Test
    public void restApiInfoHtmlWriterSettingsBuilderShouldSetCorrectTypeTreeWriterConstructor() {
        BiFunction<Appendable, Integer, ? extends AbstractTypeTreeWriter> typeTreeWriterConstructor =
                (a, i) -> mock(AbstractTypeTreeWriter.class);
        RestApiInfoHtmlWriterSettings settings = RestApiInfoHtmlWriterSettings.builder()
                .typeTreeWriterConstructor(typeTreeWriterConstructor)
                .build();

        assertSameObjects(typeTreeWriterConstructor, settings.typeTreeWriterConstructor);
    }

    @Test(expected = NullPointerException.class)
    public void restApiInfoHtmlWriterSettingsBuilderShouldThrowExceptionForNullTypeTreeWriterConstructor() {
        RestApiInfoHtmlWriterSettings.builder().typeTreeWriterConstructor(null);
    }

    //
    // ASSERTIONS
    //
    private static void assertSameObjects(Object expected, Object actual) {
        assertSame("same object is expected", expected, actual);
    }
}
