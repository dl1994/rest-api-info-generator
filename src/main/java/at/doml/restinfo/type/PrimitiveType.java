package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

public enum PrimitiveType implements Type {
    
    BYTE,
    SHORT,
    INT,
    LONG,
    BIGINT,
    FLOAT,
    DOUBLE,
    DECIMAL,
    CHAR,
    STRING,
    BOOLEAN,
    OBJECT;
    
    @Override
    public void write(TypeWriter writer) {
        writer.writePrimitive(this);
    }
}
