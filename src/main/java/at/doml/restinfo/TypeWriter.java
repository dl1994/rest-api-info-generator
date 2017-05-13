package at.doml.restinfo;

import at.doml.restinfo.type.PrimitiveType;

public interface TypeWriter {
    
    void writePrimitive(PrimitiveType type);
    
    void writeEnum(Enum<?>[] enumConstants);
    
    void writeBeforeArrayElementType();
    
    boolean shouldWriteArrayElementType();
    
    void writeAfterArrayElementType();
    
    void writeBeforeCollectionElementType();
    
    boolean shouldWriteCollectionElementType();
    
    void writeAfterCollectionElementType();
    
    void writeBeforeMapKeyType();
    
    boolean shouldWriteMapKeyType();
    
    void writeAfterMapKeyType();
    
    void writeBeforeMapValueType();
    
    boolean shouldWriteMapValueType();
    
    void writeAfterMapValueType();
    
    void beforeAllComplexFields();
    
    boolean shouldWriteComplexFields();
    
    void beforeComplexField(String fieldName);
    
    boolean shouldWriteComplexFieldType();
    
    void afterComplexField(String fieldName);
    
    void afterAllComplexFields();
    
    void writeCustom(TypeInformation typeInformation);
}
