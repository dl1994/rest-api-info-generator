package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;
import java.util.function.Consumer;
import java.util.function.Function;

final class TypeUtils {

    private TypeUtils() {
        // No instances of this class are possible
    }

    static void conditionalVisitForType(TypeVisitor visitor, VisitableType type, Consumer<TypeVisitor> visitBefore,
                                        Function<TypeVisitor, Boolean> visitTypeCondition,
                                        Consumer<TypeVisitor> visitAfter) {
        conditionalVisit(visitor, visitBefore, visitTypeCondition, type, visitAfter);
    }

    static void conditionalVisit(TypeVisitor visitor, Consumer<TypeVisitor> visitBefore,
                                 Function<TypeVisitor, Boolean> condition, Consumer<TypeVisitor> onCondition,
                                 Consumer<TypeVisitor> visitAfter) {
        visitBefore.accept(visitor);

        if (condition.apply(visitor)) {
            onCondition.accept(visitor);
        }

        visitAfter.accept(visitor);
    }
}
