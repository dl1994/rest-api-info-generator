package at.doml.restinfo;

import java.util.function.Predicate;

/**
 * Internal class which contains helper methods for {@link at.doml.restinfo} package.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 */
final class PackageUtils {

    private PackageUtils() {
        // No instances of this class are possible
    }

    static int requireNonNegative(int value, String message) {
        return requireCondition(value, v -> v >= 0, message);
    }

    static <T> T requireCondition(T value, Predicate<T> condition, String message) {
        if (!condition.test(value)) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
