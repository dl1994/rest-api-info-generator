package at.doml.restinfo.writer;

import at.doml.restinfo.type.SimpleType;
import at.doml.restinfo.type.TypeInformation;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class HtmlJsonTypeTreeWriter extends AbstractTypeTreeWriter {

    //
    // CONSTANTS
    //
    private static final int DEFAULT_INDENT_SPACING = 4;
    private static final String INDENT_SPACING_NON_NEGATIVE = "indentSpacing must not be negative";
    private static final String OBJECT_STRING = "{}";
    private static final String NOTHING_STRING = "<b class=\"keyword\">null</b>";
    private static final String NUMBER_STRING = "<b class=\"number\">0</b>";
    private static final String DECIMAL_STRING = "<b class=\"number\">0.0</b>";
    private static final String BOOLEAN_STRING = "<b class=\"keyword\">true</b> | <b class=\"keyword\">false</b>";
    private static final String STRING_STRING = "<b class=\"string\">\"string\"</b>";
    private static final String CHARACTER_STRING = "<b class=\"string\">'c'</b>";
    private static final String NEWLINE = "\n";
    private static final String SEPARATOR = ",";
    private static final String INDENT_SPACE = " ";
    private static final String COMPLEX_FIELD_QUOTATION = "\"";
    private static final String MORE_ELEMENTS_ELEMENT = "...";
    private static final String SEPARATOR_SPACE = SEPARATOR + ' ';
    private static final String SEPARATOR_NEWLINE = SEPARATOR + NEWLINE;
    private static final String ENUM_OPENING_ELEMENT = "<b class=\"keyword\">enum</b>(";
    private static final String ENUM_CLOSING_ELEMENT = ")";
    private static final String ARRAY_OPENING_ELEMENT = "[";
    private static final String ARRAY_CLOSING_ELEMENT = SEPARATOR_SPACE + MORE_ELEMENTS_ELEMENT + ']';
    private static final String COLLECTION_OPENING_ELEMENT = ARRAY_OPENING_ELEMENT;
    private static final String COLLECTION_CLOSING_ELEMENT = ARRAY_CLOSING_ELEMENT;
    private static final String KEY_VALUE_SEPARATOR = ": ";
    private static final String COMPLEX_OPENING_ELEMENT = "{";
    private static final String COMPLEX_CLOSING_ELEMENT = "}";
    private static final String MAP_OPENING_ELEMENT = COMPLEX_OPENING_ELEMENT;
    private static final String MAP_CLOSING_ELEMENT = COMPLEX_CLOSING_ELEMENT;
    private static final Map<SimpleType, String> SIMPLE_TYPE_MAPPINGS = new EnumMap<>(SimpleType.class);

    static {
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BYTE, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.SHORT, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.INT, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.LONG, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BIGINT, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.FLOAT, DECIMAL_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.DOUBLE, DECIMAL_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.DECIMAL, DECIMAL_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.CHAR, CHARACTER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.STRING, STRING_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOOLEAN, BOOLEAN_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.OBJECT, OBJECT_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.VOID, NOTHING_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_BYTE, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_SHORT, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_INT, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_LONG, NUMBER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_FLOAT, DECIMAL_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_DOUBLE, DECIMAL_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_CHAR, CHARACTER_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_BOOLEAN, BOOLEAN_STRING);
        SIMPLE_TYPE_MAPPINGS.put(SimpleType.BOXED_VOID, NOTHING_STRING);
    }

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    protected int indentLevel;
    private final int indentSpacing;

    public HtmlJsonTypeTreeWriter(Appendable stringAppender) {
        this(stringAppender, DEFAULT_INDENT_SPACING);
    }

    public HtmlJsonTypeTreeWriter(Appendable stringAppender, int indentSpacing) {
        super(stringAppender);
        this.indentSpacing = requireNonNegative(indentSpacing, INDENT_SPACING_NON_NEGATIVE);
    }

    //
    // HELPER METHODS
    //
    private static int requireNonNegative(int value, String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    //
    // INSTANCE METHODS
    //
    @Override
    protected void writeSimple(SimpleType type) throws IOException {
        this.stringAppender.append(SIMPLE_TYPE_MAPPINGS.get(type));
    }

    @Override
    protected void writeEnum(Enum<?>[] enumConstants) throws IOException {
        this.stringAppender.append(ENUM_OPENING_ELEMENT);

        if (enumConstants.length > 0) {
            int length = enumConstants.length - 1;

            for (int i = 0; i < length; i++) {
                this.stringAppender.append(enumConstants[i].toString());
                this.stringAppender.append(SEPARATOR_SPACE);
            }

            this.stringAppender.append(enumConstants[length].toString());
        }

        this.stringAppender.append(ENUM_CLOSING_ELEMENT);
    }

    @Override
    protected void writeBeforeArrayElementType() throws IOException {
        this.stringAppender.append(ARRAY_OPENING_ELEMENT);
    }

    @Override
    public boolean shouldVisitArrayElementType() {
        return true;
    }

    @Override
    protected void writeAfterArrayElementType() throws IOException {
        this.stringAppender.append(ARRAY_CLOSING_ELEMENT);
    }

    @Override
    protected void writeBeforeCollectionElementType() throws IOException {
        this.stringAppender.append(COLLECTION_OPENING_ELEMENT);
    }

    @Override
    public boolean shouldVisitCollectionElementType() {
        return true;
    }

    @Override
    protected void writeAfterCollectionElementType() throws IOException {
        this.stringAppender.append(COLLECTION_CLOSING_ELEMENT);
    }

    @Override
    protected void writeBeforeMapKeyType() throws IOException {
        this.stringAppender.append(MAP_OPENING_ELEMENT);
        this.stringAppender.append(NEWLINE);
        this.indentLevel += 1;
        this.indent();
    }

    @Override
    public boolean shouldVisitMapKeyType() {
        return true;
    }

    @Override
    protected void writeAfterMapKeyType() throws IOException {
        this.stringAppender.append(KEY_VALUE_SEPARATOR);
    }

    @Override
    protected void writeBeforeMapValueType() throws IOException {
        // No action required before visiting key type
    }

    @Override
    public boolean shouldVisitMapValueType() {
        return true;
    }

    @Override
    protected void writeAfterMapValueType() throws IOException {
        this.stringAppender.append(SEPARATOR_NEWLINE);
        this.indent();
        this.indentLevel -= 1;
        this.stringAppender.append(MORE_ELEMENTS_ELEMENT);
        this.stringAppender.append(NEWLINE);
        this.indent();
        this.stringAppender.append(MAP_CLOSING_ELEMENT);
    }

    @Override
    protected void writeBeforeAllComplexFields() throws IOException {
        this.stringAppender.append(COMPLEX_OPENING_ELEMENT);
        this.stringAppender.append(NEWLINE);
        this.indentLevel += 1;
    }

    @Override
    public boolean shouldVisitComplexFields() {
        return true;
    }

    @Override
    protected void writeBeforeComplexField(String fieldName) throws IOException {
        this.indent();
        this.stringAppender.append(COMPLEX_FIELD_QUOTATION);
        this.stringAppender.append(fieldName);
        this.stringAppender.append(COMPLEX_FIELD_QUOTATION);
        this.stringAppender.append(KEY_VALUE_SEPARATOR);
    }

    @Override
    public boolean shouldVisitComplexFieldType(String fieldName) {
        return true;
    }

    @Override
    protected void writeAfterComplexField(String fieldName) throws IOException {
        this.stringAppender.append(NEWLINE);
    }

    @Override
    protected void writeAfterAllComplexFields() throws IOException {
        this.indentLevel -= 1;
        this.indent();
        this.stringAppender.append(COMPLEX_CLOSING_ELEMENT);
    }

    @Override
    protected void writeCustom(TypeInformation customTypeInformation) throws IOException {
        this.stringAppender.append(customTypeInformation.toString());
    }

    @Override
    protected void writeUnknown(TypeInformation unknownTypeInformation) throws IOException {
        this.stringAppender.append(unknownTypeInformation.toString());
    }

    protected final void indent() throws IOException {
        int indent = this.indentLevel * this.indentSpacing;
        for (int i = 0; i < indent; i++) {
            this.stringAppender.append(INDENT_SPACE);
        }
    }
}
