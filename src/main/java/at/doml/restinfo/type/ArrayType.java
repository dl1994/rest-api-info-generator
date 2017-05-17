package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

final class ArrayType extends CollectionOrArrayType {

    ArrayType() {
        this(null);
    }

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
