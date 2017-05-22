package at.doml.restinfo.type;

import java.util.function.Consumer;

/**
 * Interface which defines an element in type tree which can be visited by <code>TypeVisitor</code>. In the
 * <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor design pattern</a>, this interface represents the
 * <code>Element</code> and <code>TypeVisitor</code> interface represents the <code>Visitor</code>. As such, this
 * interface defines a single method: <code>accept(TypeVisitor)</code>.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
@FunctionalInterface
public interface VisitableType extends Consumer<TypeVisitor> {

    /**
     * Called when <code>typeVisitor</code> visits this object. This method ensures that <code>typeVisitor</code>
     * performs desired operation(s) when visiting this object.
     *
     * @param typeVisitor reference to the visitor which visits this object (must not be <code>null</code>)
     * @throws NullPointerException if provided parameter is <code>null</code>
     */
    @Override
    void accept(TypeVisitor typeVisitor);
}
