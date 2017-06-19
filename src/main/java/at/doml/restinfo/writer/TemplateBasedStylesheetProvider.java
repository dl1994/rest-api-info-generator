package at.doml.restinfo.writer;

import java.io.IOException;

abstract class TemplateBasedStylesheetProvider implements StylesheetProvider {

    private final String string;

    TemplateBasedStylesheetProvider(String string) {
        this.string = string;
    }

    @Override
    public final void writeStylesheet(Appendable stringAppender) throws IOException {
        stringAppender.append(String.format(this.getTemplate(), this.string));
    }

    abstract String getTemplate();
}
