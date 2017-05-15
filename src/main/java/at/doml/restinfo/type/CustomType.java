package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;

final class CustomType implements WritableType {
    
    final Class<?> customClass;
    
    CustomType(Class<?> customClass) {
        this.customClass = customClass;
    }
    
    @Override
    public void write(TypeWriter writer) {
        writer.writeCustom(this.customClass);
    }
}
