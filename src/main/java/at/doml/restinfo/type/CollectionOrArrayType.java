package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

abstract class CollectionOrArrayType implements Type {
    
    private final Type elementType;
    
    CollectionOrArrayType(Type elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public final void write(TypeWriter writer) {
        TypeUtils.conditionalWrite(
                writer, this.elementType,
                this::writeBefore,
                this::shouldWriteElementType,
                this::writeAfter
        );
    }
    
    abstract void writeBefore(TypeWriter writer);
    
    abstract boolean shouldWriteElementType(TypeWriter writer);
    
    abstract void writeAfter(TypeWriter writer);
}
