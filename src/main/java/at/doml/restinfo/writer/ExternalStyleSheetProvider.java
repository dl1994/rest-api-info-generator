package at.doml.restinfo.writer;

public final class ExternalStyleSheetProvider extends TemplateBasedStylesheetProvider {

    static final String TEMPLATE = "<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">\n";

    public ExternalStyleSheetProvider(String link) {
        super(link);
    }

    @Override
    String getTemplate() {
        return TEMPLATE;
    }
}
