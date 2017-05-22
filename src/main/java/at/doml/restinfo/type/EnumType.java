package at.doml.restinfo.type;

/**
 * Class which represents an <code>enum</code> in type tree. When <code>TypeVisitor</code> object visits an instance of
 * this class, {@link TypeVisitor#visitEnum(Enum[])} method will be called on the visitor object, passing it a
 * reference to the array which contains constants of the visited <code>enum</code>.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
final class EnumType implements VisitableType {

    /**
     * Constants of the enum.
     */
    final Enum<?>[] constants;

    /**
     * Constructs an object with specified enum constants.
     *
     * @param constants constants of the enum represented by this object
     */
    EnumType(Enum<?>[] constants) {
        this.constants = constants.clone();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitEnum(this.constants.clone());
    }
}
