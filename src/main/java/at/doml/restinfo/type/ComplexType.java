package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;
import java.util.HashMap;
import java.util.Map;

final class ComplexType implements Type {
    
    private final Map<String, Type> fields = new HashMap<>();
    
    void addField(String fieldName, Type fieldType) {
        this.fields.put(fieldName, fieldType);
    }
    
    @Override
    public void write(TypeWriter writer) {
        TypeUtils.conditionalWrite(
                writer,
                TypeWriter::writeBeforeAllComplexFields,
                TypeWriter::shouldWriteComplexFields,
                this::writeFields,
                TypeWriter::writeAfterAllComplexFields
        );
    }
    
    private void writeFields(TypeWriter writer) {
        this.fields.forEach((fieldName, fieldType) ->
                TypeUtils.conditionalWrite(
                        writer, fieldType,
                        w -> w.writeBeforeComplexField(fieldName),
                        TypeWriter::shouldWriteComplexFieldType,
                        w -> w.writeAfterComplexField(fieldName)
                )
        );
    }
}
