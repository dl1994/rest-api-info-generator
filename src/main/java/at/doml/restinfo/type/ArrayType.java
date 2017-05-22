package at.doml.restinfo.type;

/**
 * Class which represents an array in type tree. When <code>TypeVisitor</code> object visits an instance of this
 * class, {@link TypeVisitor#visitBeforeArrayElementType()}, {@link TypeVisitor#shouldVisitArrayElementType()} and
 * {@link TypeVisitor#visitAfterArrayElementType()} methods will be called on the visitor object, in that order. If
 * {@link TypeVisitor#shouldVisitArrayElementType()} returns <code>true</code>, then the visitor will also visit child
 * element of this array by calling {@link VisitableType#accept(TypeVisitor)} on the child type, passing the reference
 * to the visitor object.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
final class ArrayType extends CollectionOrArrayType {

    /**
     * Constructs an object with specified child element type.
     *
     * @param elementType type of child element of this object
     */
    ArrayType(VisitableType elementType) {
        super(elementType);
    }

    @Override
    void visitBefore(TypeVisitor visitor) {
        visitor.visitBeforeArrayElementType();
    }

    @Override
    boolean shouldVisitElementType(TypeVisitor visitor) {
        return visitor.shouldVisitArrayElementType();
    }

    @Override
    void visitAfter(TypeVisitor visitor) {
        visitor.visitAfterArrayElementType();
    }
}
