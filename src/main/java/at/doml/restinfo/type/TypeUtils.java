package at.doml.restinfo.type;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Internal class which contains helper methods for <code>VisitableType</code> and <code>TypeVisitor</code>.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see VisitableType
 */
final class TypeUtils {

    private TypeUtils() {
        // No instances of this class are possible
    }

    /**
     * Calls <code>visitBefore</code>, <code>visitTypeCondition</code> and <code>visitAfter</code> functions, in that
     * order, passing them the <code>visitor</code> object reference. If <code>visitTypeCondition</code> function
     * returns <code>true</code>, then {@link VisitableType#accept(TypeVisitor)} method will be called on provided
     * <code>type</code> object, passing it the <code>visitor</code> object reference.
     * {@link VisitableType#accept(TypeVisitor)} method will be called after <code>visitBefore</code> function and
     * before <code>visitAfter</code> function.
     *
     * @param visitor            reference to the visitor object which will be passed to the functions
     * @param type               reference to the type on which {@link VisitableType#accept(TypeVisitor)} method will be
     *                           called if <code>visitTypeCondition</code> function returns <code>true</code>
     * @param visitBefore        first function to call, before all other functions
     * @param visitTypeCondition condition which will determine if {@link VisitableType#accept(TypeVisitor)} method is
     *                           called
     * @param visitAfter         last function to call, after all other functions
     */
    static void conditionalVisitForType(TypeVisitor visitor, VisitableType type, Consumer<TypeVisitor> visitBefore,
                                        Function<TypeVisitor, Boolean> visitTypeCondition,
                                        Consumer<TypeVisitor> visitAfter) {
        conditionalVisit(visitor, visitBefore, visitTypeCondition, type, visitAfter);
    }

    /**
     * Calls <code>visitBefore</code>, <code>condition</code> and <code>visitAfter</code> functions, in that order,
     * passing them the <code>visitor</code> object reference. If <code>condition</code> function returns
     * <code>true</code>, then <code>onCondition</code> function will be called with <code>visitor</code> object
     * reference. <code>onCondition</code> function will be called after <code>visitBefore</code> function and before
     * <code>visitAfter</code> function.
     *
     * @param visitor     reference to the visitor object which will be passed to the functions
     * @param visitBefore first function to call, before all other functions
     * @param condition   condition which will determine if <code>onCondition</code> function is called
     * @param onCondition function to call if <code>condition</code> function returns <code>true</code>
     * @param visitAfter  last function to call, after all other functions
     */
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
