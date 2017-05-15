package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;

public interface VisitableType {
    
    void visit(TypeVisitor visitor);
}
