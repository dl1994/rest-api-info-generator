package at.doml.restinfo.writer;

import at.doml.restinfo.ControllerInfo;
import at.doml.restinfo.type.VisitableType;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    final BiFunction<Appendable, Integer, ? extends AbstractTypeTreeWriter> typeTreeWriterConstructor;

    private RestApiInfoHtmlWriterSettings(int indentSpacing, Set<PrintSection> sectionsToPrint,
                                          StylesheetProvider stylesheetProvider,
                                          BiFunction<Appendable, Integer, ? extends AbstractTypeTreeWriter>
                                                  typeTreeWriterConstructor) {
        this.indentSpacing = indentSpacing;
        this.sectionsToPrint = EnumSet.copyOf(sectionsToPrint);
        this.stylesheetProvider = stylesheetProvider;
        this.typeTreeWriterConstructor = typeTreeWriterConstructor;
    }

    //
    // UTIL CLASSES AND INTERFACES
    //
    public enum PrintSection {
        REQUEST_BODY("Request body", ControllerInfo::getRequestBodyTypeTree),
        RESPONSE_BODY("Response body", ControllerInfo::getResponseBodyTypeTree),
        PATH_VARIABLES("Path variables", ControllerInfo::getPathVariablesTypeTree),
        MODEL_ATTRIBUTES("Query parameters", ControllerInfo::getQueryParametersTypeTree);

        private final String sectionName;
        private final Function<ControllerInfo, VisitableType> typeTreeGetter;

        PrintSection(String sectionName, Function<ControllerInfo, VisitableType> typeTreeGetter) {
            this.sectionName = sectionName;
            this.typeTreeGetter = typeTreeGetter;
        }

        VisitableType getTypeTree(ControllerInfo controllerInfo) {
            return this.typeTreeGetter.apply(controllerInfo);
        }

        String getSectionName() {
            return this.sectionName;
        }
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
        private static final String TYPE_TREE_WRITER_CONSTRUCTOR_NOT_NULL = "typeTreeWriterConstructor" + NOT_NULL;
        private static final int DEFAULT_INDENT_SPACING = 4;
        private static final StylesheetProvider DEFAULT_STYLESHEET_PROVIDER = new InternalStylesheetProvider(
                PackageUtils.loadResource("default.css")
        );

        //
        // CONSTRUCTORS AND MEMBER VARIABLES
        //
        private int indentSpacing;
        private StylesheetProvider stylesheetProvider;
        private BiFunction<Appendable, Integer, ? extends AbstractTypeTreeWriter> typeTreeWriterConstructor;
        private final Set<PrintSection> sectionsToPrint;

        private Builder() {
            this.indentSpacing = DEFAULT_INDENT_SPACING;
            this.stylesheetProvider = DEFAULT_STYLESHEET_PROVIDER;
            this.typeTreeWriterConstructor = HtmlJsonTypeTreeWriter::new;
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

        public Builder typeTreeWriterConstructor(BiFunction<Appendable, Integer, ? extends AbstractTypeTreeWriter>
                                                         typeTreeWriterConstructor) {
            this.typeTreeWriterConstructor = Objects.requireNonNull(
                    typeTreeWriterConstructor, TYPE_TREE_WRITER_CONSTRUCTOR_NOT_NULL);
            return this;
        }

        public RestApiInfoHtmlWriterSettings build() {
            return new RestApiInfoHtmlWriterSettings(
                    this.indentSpacing,
                    this.sectionsToPrint,
                    this.stylesheetProvider,
                    this.typeTreeWriterConstructor
            );
        }
    }
}
