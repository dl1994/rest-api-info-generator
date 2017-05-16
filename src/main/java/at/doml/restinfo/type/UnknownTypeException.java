package at.doml.restinfo.type;

public final class UnknownTypeException extends RuntimeException {
    
    private final String typeName;
    private static final long serialVersionUID = -2062036712705649611L;
    
    public UnknownTypeException(String typeName, Throwable cause) {
        super("no class for type: " + typeName, cause);
        this.typeName = typeName;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
}
