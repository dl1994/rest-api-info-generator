package at.doml.restinfo.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Class which represents a complex type in type tree. When <code>TypeVisitor</code> object visits an instance of this
 * class, {@link TypeVisitor#visitBeforeAllComplexFields()}, {@link TypeVisitor#shouldVisitComplexFields()}
 * and {@link TypeVisitor#visitAfterAllComplexFields()} methods will be called on the visitor object, in that
 * order. If {@link TypeVisitor#shouldVisitComplexFields()} returns <code>true</code>, then the visitor will
 * also visit all child elements of this complex type by calling {@link TypeVisitor#visitBeforeComplexField(String)},
 * {@link TypeVisitor#shouldVisitComplexFieldType(String)} and {@link TypeVisitor#visitAfterComplexField(String)}
 * methods on the visitor object, in that order. <code>String</code> passed to <code>visitBeforeComplexField</code> and
 * <code>visitAfterComplexField</code> methods is the name of the child field visited. If
 * {@link TypeVisitor#shouldVisitComplexFieldType(String)} returns <code>true</code>, then each child field will be
 * visited by calling {@link VisitableType#accept(TypeVisitor)} on the child type passing the reference to the visitor
 * object.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
final class ComplexType implements VisitableType {

    /**
     * Fields of this complex type.
     */
    final Map<String, VisitableType> fields = new HashMap<>();

    /**
     * Adds a field to this complex type. Field is composed of its name and child element.
     *
     * @param fieldName name of the field
     * @param fieldType type of field child element
     */
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
                        v -> v.shouldVisitComplexFieldType(fieldName),
                        v -> v.visitAfterComplexField(fieldName)
                )
        );
    }
}
