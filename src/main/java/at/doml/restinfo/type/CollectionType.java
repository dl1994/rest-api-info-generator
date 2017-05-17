package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

final class CollectionType extends CollectionOrArrayType {

    CollectionType() {
        this(null);
    }

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
