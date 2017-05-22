package at.doml.restinfo.type;

/**
 * Exception thrown by <code>TypeTreeGenerator</code> for types which cannot be found by class loader when unknown type
 * handling is set to {@link TypeTreeGenerator.UnknownTypeHandling#THROW_EXCEPTION}. When thrown by the
 * <code>TypeTreeGenerator</code>, cause will always be an instance of {@link ClassNotFoundException}.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeTreeGenerator
 */
public final class UnknownTypeException extends RuntimeException {

    private final String typeName;
    private static final long serialVersionUID = -2062036712705649611L;

    /**
     * Constructs an exception with specified type name and cause.
     *
     * @param typeName name of the unknown type which caused this exception.
     * @param cause    cause of this exception.
     */
    public UnknownTypeException(String typeName, Throwable cause) {
        super("no class for type: " + typeName, cause);
        this.typeName = typeName;
    }

    /**
     * Fetches the name of the type which caused this exception.
     *
     * @return name of the type which caused this exception
     */
    public String getTypeName() {
        return this.typeName;
    }
}
