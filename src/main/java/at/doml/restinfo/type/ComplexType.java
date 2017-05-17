package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;
import java.util.HashMap;
import java.util.Map;

final class ComplexType implements VisitableType {

    final Map<String, VisitableType> fields = new HashMap<>();

    void addField(String fieldName, VisitableType fieldType) {
        this.fields.put(fieldName, fieldType);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        TypeUtils.conditionalVisit(
                visitor,
                TypeVisitor::visitBeforeAllComplexFields,
                TypeVisitor::shouldVisitComplexFields,
                this::visitFields,
                TypeVisitor::visitAfterAllComplexFields
        );
    }

    private void visitFields(TypeVisitor visitor) {
        this.fields.forEach((fieldName, fieldType) ->
                TypeUtils.conditionalVisitForType(
                        visitor, fieldType,
                        v -> v.visitBeforeComplexField(fieldName),
                        TypeVisitor::shouldVisitComplexFieldType,
                        v -> v.visitAfterComplexField(fieldName)
                )
        );
    }
}
