package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

final class CustomType implements VisitableType {
    
    final Class<?> customClass;
    
    CustomType(Class<?> customClass) {
        this.customClass = customClass;
    }
    
    @Override
    public void visit(TypeVisitor visitor) {
        visitor.visitCustom(this.customClass);
    }
}
