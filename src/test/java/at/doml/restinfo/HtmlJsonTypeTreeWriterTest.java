package at.doml.restinfo;

import at.doml.restinfo.type.SimpleType;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public final class HtmlJsonTypeTreeWriterTest {

    //
    // CONSTANTS
    //
    private static final String OBJECT_HTML = "{}";
    private static final String NOTHING_HTML = "<b class=\"keyword\">null</b>";
    private static final String NUMBER_HTML = "<b class=\"number\">0</b>";
    private static final String DECIMAL_HTML = "<b class=\"number\">0.0</b>";
    private static final String BOOLEAN_HTML = "<b class=\"keyword\">true</b> | <b class=\"keyword\">false</b>";
    private static final String STRING_HTML = "<b class=\"string\">\"string\"</b>";
    private static final String CHARACTER_HTML = "<b class=\"string\">'c'</b>";
    private static final String EMPTY_ENUM_HTML = "<b class=\"keyword\">enum</b>()";
    private static final String TEST_ENUM_HTML = "<b class=\"keyword\">enum</b>(A, B, C)";
    private static final String ARRAY_HTML = "[, ...]";
    private static final String COLLECTION_HTML = ARRAY_HTML;
    private static final String MAP_HTML = "{\n    : ,\n    ...\n}";
    private static final String FIELD_NAME = "fieldName";
    private static final String COMPLEX_HTML = "{\n    \"" + FIELD_NAME + "\": \n}";
    private static final String TEST_TYPE = "int";
    private static final String CUSTOM_HTML = "int[]";
    private static final String UNKNOWN_HTML = CUSTOM_HTML;
    private static final TypeInformation TEST_TYPE_INFORMATION = new TypeInformation(
            TEST_TYPE, new TypeInformation[0], 1
    );

    //
    // TEST VARIABLES
    //
    private final StringBuilder builder = new StringBuilder();
    private final HtmlJsonTypeTreeWriter writer = new HtmlJsonTypeTreeWriter(this.builder);

    //
    // TESTS
    //
    @Test
    public void writeSimpleShouldWriteCorrectHtml() throws IOException {
        // Numbers
        this.writer.writeSimple(SimpleType.BYTE);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.SHORT);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.INT);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.LONG);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.BIGINT);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.BOXED_BYTE);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.BOXED_SHORT);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.BOXED_INT);
        this.assertHtml(NUMBER_HTML);
        this.writer.writeSimple(SimpleType.BOXED_LONG);
        this.assertHtml(NUMBER_HTML);
        // Decimals
        this.writer.writeSimple(SimpleType.FLOAT);
        this.assertHtml(DECIMAL_HTML);
        this.writer.writeSimple(SimpleType.DOUBLE);
        this.assertHtml(DECIMAL_HTML);
        this.writer.writeSimple(SimpleType.DECIMAL);
        this.assertHtml(DECIMAL_HTML);
        this.writer.writeSimple(SimpleType.BOXED_FLOAT);
        this.assertHtml(DECIMAL_HTML);
        this.writer.writeSimple(SimpleType.BOXED_DOUBLE);
        this.assertHtml(DECIMAL_HTML);
        // Characters
        this.writer.writeSimple(SimpleType.CHAR);
        this.assertHtml(CHARACTER_HTML);
        this.writer.writeSimple(SimpleType.BOXED_CHAR);
        this.assertHtml(CHARACTER_HTML);
        // String
        this.writer.writeSimple(SimpleType.STRING);
        this.assertHtml(STRING_HTML);
        // Boolean
        this.writer.writeSimple(SimpleType.BOOLEAN);
        this.assertHtml(BOOLEAN_HTML);
        this.writer.writeSimple(SimpleType.BOXED_BOOLEAN);
        this.assertHtml(BOOLEAN_HTML);
        // Object
        this.writer.writeSimple(SimpleType.OBJECT);
        this.assertHtml(OBJECT_HTML);
        // Nothing
        this.writer.writeSimple(SimpleType.VOID);
        this.assertHtml(NOTHING_HTML);
        this.writer.writeSimple(SimpleType.BOXED_VOID);
        this.assertHtml(NOTHING_HTML);
    }

    @Test
    public void writeEnumShouldWriteCorrectHtml() throws IOException {
        this.writer.writeEnum(new Enum[0]);
        this.assertHtml(EMPTY_ENUM_HTML);
        this.writer.writeEnum(TestEnum.values());
        this.assertHtml(TEST_ENUM_HTML);
    }

    @Test
    public void writeArrayShouldWriteCorrectHtml() throws IOException {
        this.writer.writeBeforeArrayElementType();
        this.writer.writeAfterArrayElementType();
        this.assertHtml(ARRAY_HTML);
    }

    @Test
    public void writeCollectionShouldWriteCorrectHtml() throws IOException {
        this.writer.writeBeforeCollectionElementType();
        this.writer.writeAfterCollectionElementType();
        this.assertHtml(COLLECTION_HTML);
    }

    @Test
    public void writeMapShouldWriteCorrectHtml() throws IOException {
        this.writer.writeBeforeMapKeyType();
        this.writer.writeAfterMapKeyType();
        this.writer.writeBeforeMapValueType();
        this.writer.writeAfterMapValueType();
        this.assertHtml(MAP_HTML);
    }

    @Test
    public void writeComplexShouldWriteCorrectHtml() throws IOException {
        this.writer.writeBeforeAllComplexFields();
        this.writer.writeBeforeComplexField(FIELD_NAME);
        this.writer.writeAfterComplexField(FIELD_NAME);
        this.writer.writeAfterAllComplexFields();
        this.assertHtml(COMPLEX_HTML);
    }

    @Test
    public void writeCustomShouldWriteCorrectHtml() throws IOException {
        this.writer.writeCustom(TEST_TYPE_INFORMATION);
        this.assertHtml(CUSTOM_HTML);
    }

    @Test
    public void writeUnknownShouldWriteCorrectHtml() throws IOException {
        this.writer.writeUnknown(TEST_TYPE_INFORMATION);
        this.assertHtml(UNKNOWN_HTML);
    }

    @Test
    public void allShouldVisitMethodsShouldReturnTrue() {
        assertTrue(this.writer.shouldVisitArrayElementType());
        assertTrue(this.writer.shouldVisitCollectionElementType());
        assertTrue(this.writer.shouldVisitMapKeyType());
        assertTrue(this.writer.shouldVisitMapValueType());
        assertTrue(this.writer.shouldVisitComplexFields());
        assertTrue(this.writer.shouldVisitComplexFieldType(null));
    }

    //
    // PRIVATE CLASSES
    //
    private enum TestEnum {
        @SuppressWarnings("unused")
        A,
        @SuppressWarnings("unused")
        B,
        @SuppressWarnings("unused")
        C
    }

    //
    // ASSERTIONS
    //
    private void assertHtml(String expectedHtml) {
        assertEquals("provided html is not correct", expectedHtml, this.builder.toString());
        this.builder.delete(0, this.builder.length());
    }

    private static void assertTrue(boolean value) {
        Assert.assertTrue("method should have returned 'true'", value);
    }
}
