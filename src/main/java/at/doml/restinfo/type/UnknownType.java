package at.doml.restinfo.type;

/**
 * Class which represents unknown type in type tree. When <code>TypeVisitor</code> object visits an instance of this
 * class, {@link TypeVisitor#visitUnknown(TypeInformation)} method will be called on the visitor object, passing it a
 * reference to the <code>TypeInformation</code> object which contains additional information about this unknown type.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 * @see TypeInformation
 */
final class UnknownType extends CustomOrUnknownType {

    /**
     * Constructs an object with specified type information.
     *
     * @param unknownTypeInformation additional type information about this type
     */
    UnknownType(TypeInformation unknownTypeInformation) {
        super(unknownTypeInformation);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitUnknown(this.typeInformation);
    }
}
