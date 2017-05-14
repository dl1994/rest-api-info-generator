package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

final class MapType implements WritableType {
    
    private final WritableType keyType;
    private final WritableType valueType;
    
    MapType(WritableType keyType, WritableType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    @Override
    public void write(TypeWriter writer) {
        TypeUtils.conditionalWrite(
                writer, this.keyType,
                TypeWriter::writeBeforeMapKeyType,
                TypeWriter::shouldWriteMapKeyType,
                TypeWriter::writeAfterMapKeyType
        );
        TypeUtils.conditionalWrite(
                writer, this.valueType,
                TypeWriter::writeBeforeMapValueType,
                TypeWriter::shouldWriteMapValueType,
                TypeWriter::writeAfterMapValueType
        );
    }
}
