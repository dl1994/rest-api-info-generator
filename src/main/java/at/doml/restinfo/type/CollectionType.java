package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

final class CollectionType extends CollectionOrArrayType {
    
    CollectionType(Type elementType) {
        super(elementType);
    }
    
    @Override
    void writeBefore(TypeWriter writer) {
        writer.writeBeforeCollectionElementType();
    }
    
    @Override
    boolean shouldWriteElementType(TypeWriter writer) {
        return writer.shouldWriteCollectionElementType();
    }
    
    @Override
    void writeAfter(TypeWriter writer) {
        writer.writeAfterCollectionElementType();
    }
}
