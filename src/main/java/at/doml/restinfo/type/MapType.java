package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

final class MapType implements VisitableType {
    
    final VisitableType keyType;
    final VisitableType valueType;
    
    MapType() {
        this(null, null);
    }
    
    MapType(VisitableType keyType, VisitableType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    @Override
    public void visit(TypeVisitor visitor) {
        TypeUtils.conditionalVisit(
                visitor, this.keyType,
                TypeVisitor::visitBeforeMapKeyType,
                TypeVisitor::shouldVisitMapKeyType,
                TypeVisitor::visitAfterMapKeyType
        );
        TypeUtils.conditionalVisit(
                visitor, this.valueType,
                TypeVisitor::visitBeforeMapValueType,
                TypeVisitor::shouldVisitMapValueType,
                TypeVisitor::visitAfterMapValueType
        );
    }
}
