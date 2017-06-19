package at.doml.restinfo.writer;

final class PackageUtils {

    private static final String INDENT_SPACING_NON_NEGATIVE = "indentSpacing must not be negative";

    private PackageUtils() {
        // No instances of this class are possible
    }

    static int requireNonNegativeIndentSpacing(int value) {
        if (value < 0) {
            throw new IllegalArgumentException(INDENT_SPACING_NON_NEGATIVE);
        }

        return value;
    }
}
