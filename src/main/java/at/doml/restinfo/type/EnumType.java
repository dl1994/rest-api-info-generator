package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

final class EnumType implements VisitableType {

    final Enum<?>[] constants;

    EnumType(Enum<?>[] constants) {
        this.constants = constants.clone();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitEnum(this.constants.clone());
    }
}
