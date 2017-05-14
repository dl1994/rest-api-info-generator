package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

abstract class CollectionOrArrayType implements WritableType {
    
    private final WritableType elementType;
    
    CollectionOrArrayType(WritableType elementType) {
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
