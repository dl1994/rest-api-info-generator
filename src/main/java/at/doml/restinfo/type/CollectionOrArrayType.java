package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

abstract class CollectionOrArrayType implements VisitableType {

    final VisitableType elementType;

    CollectionOrArrayType(VisitableType elementType) {
        this.elementType = elementType;
    }

    @Override
    public final void accept(TypeVisitor visitor) {
        TypeUtils.conditionalVisitForType(
                visitor, this.elementType,
                this::visitBefore,
                this::shouldVisitElementType,
                this::visitAfter
        );
    }

    abstract void visitBefore(TypeVisitor visitor);

    abstract boolean shouldVisitElementType(TypeVisitor visitor);

    abstract void visitAfter(TypeVisitor visitor);
}
