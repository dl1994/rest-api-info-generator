package at.doml.restinfo;

import at.doml.restinfo.type.SimpleType;

public interface TypeVisitor {

    void visitSimple(SimpleType type);

    void visitEnum(Enum<?>[] enumConstants);

    void visitBeforeArrayElementType();

    boolean shouldVisitArrayElementType();

    void visitAfterArrayElementType();

    void visitBeforeCollectionElementType();

    boolean shouldVisitCollectionElementType();

    void visitAfterCollectionElementType();

    void visitBeforeMapKeyType();

    boolean shouldVisitMapKeyType();

    void visitAfterMapKeyType();

    void visitBeforeMapValueType();

    boolean shouldVisitMapValueType();

    void visitAfterMapValueType();

    void visitBeforeAllComplexFields();

    boolean shouldVisitComplexFields();

    void visitBeforeComplexField(String fieldName);

    boolean shouldVisitComplexFieldType();

    void visitAfterComplexField(String fieldName);

    void visitAfterAllComplexFields();

    void visitCustom(TypeInformation customTypeInformation);

    void visitUnknown(TypeInformation unknownTypeInformation);
}
