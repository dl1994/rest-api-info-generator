package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import at.doml.restinfo.TypeVisitor;

final class CustomType extends CustomOrUnknownType {

    CustomType(TypeInformation customTypeInformation) {
        super(customTypeInformation);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitCustom(this.typeInformation);
    }
}
