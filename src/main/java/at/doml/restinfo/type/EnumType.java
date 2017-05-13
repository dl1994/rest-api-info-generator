package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

final class EnumType implements Type {
    
    private final Enum<?>[] constants;
    
    EnumType(Enum<?>[] constants) {
        this.constants = constants;
    }
    
    @Override
    public void write(TypeWriter writer) {
        writer.writeEnum(this.constants.clone());
    }
}
