package at.doml.restinfo.type;

/**
 * Enumeration which defines simple types in type tree. Simple types are all Java primitive types (<code>byte</code>,
 * <code>short</code>, <code>int</code>, <code>long</code>, ...), their boxed counterparts (<code>java.lang.Byte</code>,
 * <code>java.lang.Short</code>, <code>java.lang.Integer</code>, ...) and classes <code>java.lang.Object</code>,
 * <code>java.lang.String</code>, <code>java.math.BigInteger</code> and <code>java.math.BigDecimal</code>. Additionally,
 * <code>void</code> and <code>java.lang.Void</code> are also considered types, and are as such represented in this
 * enumeration. Each simple type will always be represented by same constant in this enumeration. When
 * <code>TypeVisitor</code> object visits a constant of this enumeration, {@link TypeVisitor#visitSimple(SimpleType)}
 * method will be called on the visitor object, passing it a reference to the visited constant.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
public enum SimpleType implements VisitableType {

    /**
     * Represents primitive Java <code>byte</code> type.
     */
    BYTE,
    /**
     * Represents primitive Java <code>short</code> type.
     */
    SHORT,
    /**
     * Represents primitive Java <code>int</code> type.
     */
    INT,
    /**
     * Represents primitive Java <code>long</code> type.
     */
    LONG,
    /**
     * Represents <code>java.math.BigInteger</code> class.
     */
    BIGINT,
    /**
     * Represents primitive Java <code>float</code> type.
     */
    FLOAT,
    /**
     * Represents primitive Java <code>double</code> type.
     */
    DOUBLE,
    /**
     * Represents <code>java.math.BigDecimal</code> class.
     */
    DECIMAL,
    /**
     * Represents primitive Java <code>char</code> type.
     */
    CHAR,
    /**
     * Represents <code>java.lang.String</code> class.
     */
    STRING,
    /**
     * Represents primitive Java <code>boolean</code> type.
     */
    BOOLEAN,
    /**
     * Represents <code>java.lang.Object</code> class.
     */
    OBJECT,
    /**
     * Represents primitive Java <code>void</code> type.
     */
    VOID,
    /**
     * Represents boxed Java <code>byte</code> type (<code>java.lang.Byte</code>).
     */
    BOXED_BYTE,
    /**
     * Represents boxed Java <code>short</code> type (<code>java.lang.Short</code>).
     */
    BOXED_SHORT,
    /**
     * Represents boxed Java <code>int</code> type (<code>java.lang.Integer</code>).
     */
    BOXED_INT,
    /**
     * Represents boxed Java <code>long</code> type (<code>java.lang.Long</code>).
     */
    BOXED_LONG,
    /**
     * Represents boxed Java <code>float</code> type (<code>java.lang.Float</code>).
     */
    BOXED_FLOAT,
    /**
     * Represents boxed Java <code>double</code> type (<code>java.lang.Double</code>).
     */
    BOXED_DOUBLE,
    /**
     * Represents boxed Java <code>char</code> type (<code>java.lang.Character</code>).
     */
    BOXED_CHAR,
    /**
     * Represents boxed Java <code>boolean</code> type (<code>java.lang.Boolean</code>).
     */
    BOXED_BOOLEAN,
    /**
     * Represents boxed Java <code>void</code> type (<code>java.lang.Void</code>).
     */
    BOXED_VOID;

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitSimple(this);
    }
}
