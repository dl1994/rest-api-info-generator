package at.doml.restinfo.util;

public final class Util {

    private static final String NOT_NULL = " must not be null";
    public static final String TYPE_NOT_NULL = "type" + NOT_NULL;
    public static final String METHOD_FIELD_EXTRACTION_NOT_NULL = "methodFieldExtraction" + NOT_NULL;
    public static final String UNKNOWN_TYPE_HANDLING_NOT_NULL = "unknownTypeHandling" + NOT_NULL;
    public static final String TYPE_PARAMETERS_NOT_NULL = "typeParameters" + NOT_NULL;
    public static final String TYPE_PARAMETER_MAPPINGS_NOT_NULL = "typeParameterMappings" + NOT_NULL;
    public static final String ARRAY_DIMENSION_NON_NEGATIVE = "arrayDimension must not be negative";

    private Util() {
        // No instances of this class are possible
    }

    public static int requireNonNegative(int value, String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
