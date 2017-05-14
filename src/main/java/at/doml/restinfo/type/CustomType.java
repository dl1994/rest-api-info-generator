package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import at.doml.restinfo.TypeWriter;

final class CustomType implements WritableType {
    
    private final TypeInformation typeInformation;
    
    CustomType(TypeInformation typeInformation) {
        this.typeInformation = typeInformation;
    }
    
    @Override
    public void write(TypeWriter writer) {
        writer.writeCustom(this.typeInformation);
    }
}
