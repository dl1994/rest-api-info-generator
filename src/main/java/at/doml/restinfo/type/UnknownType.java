package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import at.doml.restinfo.TypeVisitor;

final class UnknownType extends CustomOrUnknownType {
    
    UnknownType(TypeInformation customTypeInformation) {
        super(customTypeInformation);
    }
    
    @Override
    public void visit(TypeVisitor visitor) {
        visitor.visitUnknown(this.typeInformation);
    }
}
