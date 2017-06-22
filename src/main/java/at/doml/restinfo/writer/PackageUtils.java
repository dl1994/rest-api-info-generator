package at.doml.restinfo.writer;

import org.springframework.web.client.ResourceAccessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

    // TODO test if this works correctly in jar
    static String loadResource(String resourceName) {
        String loadFailMessage = "unable to load resource: [" + resourceName + ']';
        InputStream cssStream = RestApiInfoHtmlWriterSettings.class.getResourceAsStream(resourceName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cssStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .reduce((l, r) -> l + '\n' + r)
                    .orElseThrow(() -> new NullPointerException("no lines to read"));
        } catch (IOException e) {
            throw new ResourceAccessException(loadFailMessage + ", reason: IOException", e);
        } catch (NullPointerException e) {
            String message = e.getMessage();
            IOException cause = new IOException(e);

            throw new ResourceAccessException(
                    loadFailMessage + (message != null ? ", reason: " + e.getMessage() : ""), cause
            );
        }
    }
}
