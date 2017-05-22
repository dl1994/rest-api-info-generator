package at.doml.restinfo.type;

/**
 * Class which represents a collection in type tree. When <code>TypeVisitor</code> object visits an instance of this
 * class, {@link TypeVisitor#visitBeforeCollectionElementType()}, {@link TypeVisitor#shouldVisitCollectionElementType()}
 * and {@link TypeVisitor#visitAfterCollectionElementType()} methods will be called on the visitor object, in that
 * order. If {@link TypeVisitor#shouldVisitCollectionElementType()} returns <code>true</code>, then the visitor will
 * also visit child element of this collection by calling {@link VisitableType#accept(TypeVisitor)} on the child type,
 * passing the reference to the visitor object.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
final class CollectionType extends CollectionOrArrayType {

    /**
     * Constructs an object with specified child element type.
     *
     * @param elementType type of child element of this object
     */
    CollectionType(VisitableType elementType) {
        super(elementType);
    }

    @Override
    void visitBefore(TypeVisitor visitor) {
        visitor.visitBeforeCollectionElementType();
    }

    @Override
    boolean shouldVisitElementType(TypeVisitor visitor) {
        return visitor.shouldVisitCollectionElementType();
    }

    @Override
    void visitAfter(TypeVisitor visitor) {
        visitor.visitAfterCollectionElementType();
    }
}
