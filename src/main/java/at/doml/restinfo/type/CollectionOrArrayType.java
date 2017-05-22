package at.doml.restinfo.type;

/**
 * Abstract class which represents array or collection in type tree.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
abstract class CollectionOrArrayType implements VisitableType {

    /**
     * Child element type of this object.
     */
    final VisitableType elementType;

    /**
     * Constructs an object with specified child element type.
     *
     * @param elementType type of child element of this object
     */
    CollectionOrArrayType(VisitableType elementType) {
        this.elementType = elementType;
    }

    @Override
    public final void accept(TypeVisitor visitor) {
        TypeUtils.conditionalVisitForType(
                visitor, this.elementType,
                this::visitBefore,
                this::shouldVisitElementType,
                this::visitAfter
        );
    }

    /**
     * Action to perform before visiting child element.
     *
     * @param visitor visitor object which will visit this object
     */
    abstract void visitBefore(TypeVisitor visitor);

    /**
     * Specifies whether visitor should visit child element.
     *
     * @param visitor visitor object which will be passed to the child element
     * @return <code>true</code> if visitor should perform an action on child element, <code>false</code> otherwise
     */
    abstract boolean shouldVisitElementType(TypeVisitor visitor);

    /**
     * Action to perform after visiting child element.
     *
     * @param visitor visitor object which will visit this object
     */
    abstract void visitAfter(TypeVisitor visitor);
}
