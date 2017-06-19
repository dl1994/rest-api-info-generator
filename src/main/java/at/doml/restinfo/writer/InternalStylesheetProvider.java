package at.doml.restinfo.writer;

public final class InternalStylesheetProvider extends TemplateBasedStylesheetProvider {

    static final String TEMPLATE = "<style>\n%s\n</style>\n";

    public InternalStylesheetProvider(String css) {
        super(css);
    }

    @Override
    String getTemplate() {
        return TEMPLATE;
    }
}
