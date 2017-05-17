package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;
import java.util.function.Consumer;

@FunctionalInterface
public interface VisitableType extends Consumer<TypeVisitor> {}
