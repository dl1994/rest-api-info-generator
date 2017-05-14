package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

final class EnumType implements WritableType {
    
    private final Enum<?>[] constants;
    
    EnumType(Enum<?>[] constants) {
        this.constants = constants.clone();
    }
    
    @Override
    public void write(TypeWriter writer) {
        writer.writeEnum(this.constants.clone());
    }
}
