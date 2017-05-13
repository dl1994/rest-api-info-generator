package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

final class ArrayType extends CollectionOrArrayType {
    
    ArrayType(Type elementType) {
        super(elementType);
    }
    
    @Override
    void writeBefore(TypeWriter writer) {
        writer.writeBeforeArrayElementType();
    }
    
    @Override
    boolean shouldWriteElementType(TypeWriter writer) {
        return writer.shouldWriteArrayElementType();
    }
    
    @Override
    void writeAfter(TypeWriter writer) {
        writer.writeAfterArrayElementType();
    }
}
