package at.doml.restinfo.writer;

import org.springframework.web.client.ResourceAccessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class RestApiInfoHtmlWriterSettings {

    //
    // CONSTANTS
    //
    public static final RestApiInfoHtmlWriterSettings DEFAULT = builder()
            .printSection(PrintSection.REQUEST_BODY)
            .printSection(PrintSection.RESPONSE_BODY)
            .printSection(PrintSection.PATH_VARIABLES)
            .printSection(PrintSection.MODEL_ATTRIBUTES)
            .build();

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    final int indentSpacing;
    final Set<PrintSection> sectionsToPrint;
    final StylesheetProvider stylesheetProvider;

    private RestApiInfoHtmlWriterSettings(int indentSpacing, Set<PrintSection> sectionsToPrint,
                                          StylesheetProvider stylesheetProvider) {
        this.indentSpacing = indentSpacing;
        this.sectionsToPrint = sectionsToPrint;
        this.stylesheetProvider = stylesheetProvider;
    }

    //
    // UTIL CLASSES AND INTERFACES
    //
    public enum PrintSection {
        REQUEST_BODY,
        RESPONSE_BODY,
        PATH_VARIABLES,
        MODEL_ATTRIBUTES
    }

    //
    // BUILDER
    //
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        //
        // CONSTANTS
        //
        private static final String NOT_NULL = " must not be null";
        private static final String PRINT_SECTION_NOT_NULL = "printSection" + NOT_NULL;
        private static final String STYLESHEET_PROVIDER_NOT_NULL = "stylesheetProvider" + NOT_NULL;
        private static final int DEFAULT_INDENT_SPACING = 4;
        private static final StylesheetProvider DEFAULT_STYLESHEET_PROVIDER = loadDefaultStylesheetProvider();

        // TODO test if this is loaded correctly in jar
        private static StylesheetProvider loadDefaultStylesheetProvider() {
            String resourceName = "default.css";
            String loadFailMessage = "unable to load resource: " + resourceName;
            InputStream cssStream = RestApiInfoHtmlWriterSettings.class.getResourceAsStream(resourceName);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cssStream, StandardCharsets.UTF_8))) {
                return new InternalStylesheetProvider(
                        reader.lines()
                                .reduce((l, r) -> l + '\n' + r)
                                .orElseThrow(RuntimeException::new)
                );
            } catch (IOException e) {
                throw new ResourceAccessException(loadFailMessage, e);
            } catch (NullPointerException ignored) {
                throw new ResourceAccessException(loadFailMessage);
            }
        }

        //
        // CONSTRUCTORS AND MEMBER VARIABLES
        //
        private int indentSpacing;
        private StylesheetProvider stylesheetProvider;
        private final Set<PrintSection> sectionsToPrint;

        private Builder() {
            this.indentSpacing = DEFAULT_INDENT_SPACING;
            this.stylesheetProvider = DEFAULT_STYLESHEET_PROVIDER;
            this.sectionsToPrint = EnumSet.noneOf(PrintSection.class);
        }

        //
        // INSTANCE METHODS
        //
        public Builder indentSpacing(int indentSpacing) {
            this.indentSpacing = PackageUtils.requireNonNegativeIndentSpacing(indentSpacing);
            return this;
        }

        public Builder printSection(PrintSection printSection) {
            this.sectionsToPrint.add(Objects.requireNonNull(printSection, PRINT_SECTION_NOT_NULL));
            return this;
        }

        public Builder stylesheetProvider(StylesheetProvider stylesheetProvider) {
            this.stylesheetProvider = Objects.requireNonNull(stylesheetProvider, STYLESHEET_PROVIDER_NOT_NULL);
            return this;
        }

        public RestApiInfoHtmlWriterSettings build() {
            return new RestApiInfoHtmlWriterSettings(this.indentSpacing, this.sectionsToPrint, this.stylesheetProvider);
        }
    }
}
