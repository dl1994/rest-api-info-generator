package at.doml.restinfo.writer;

import java.io.IOException;

@FunctionalInterface
public interface StylesheetProvider {

    void writeStylesheet(Appendable stringAppender) throws IOException;
}
