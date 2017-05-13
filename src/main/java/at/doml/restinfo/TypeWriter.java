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
    
    void writeBeforeAllComplexFields();
    
    boolean shouldWriteComplexFields();
    
    void writeBeforeComplexField(String fieldName);
    
    boolean shouldWriteComplexFieldType();
    
    void writeAfterComplexField(String fieldName);
    
    void writeAfterAllComplexFields();
    
    void writeCustom(TypeInformation typeInformation);
}
