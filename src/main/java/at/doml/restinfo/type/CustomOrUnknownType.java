package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;

abstract class CustomOrUnknownType implements VisitableType {
    
    final TypeInformation typeInformation;
    
    CustomOrUnknownType(TypeInformation typeInformation) {
        this.typeInformation = typeInformation;
    }
}
