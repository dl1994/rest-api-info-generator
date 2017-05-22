package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;

/**
 * Class which represents custom type in type tree. When <code>TypeVisitor</code> object visits an instance of this
 * class, {@link TypeVisitor#visitCustom(TypeInformation)} method will be called on the visitor object, passing it a
 * reference to the <code>TypeInformation</code> object which contains additional information about this custom type.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 * @see TypeInformation
 */
final class CustomType extends CustomOrUnknownType {

    /**
     * Constructs an object with specified type information.
     *
     * @param customTypeInformation additional type information about this type
     */
    CustomType(TypeInformation customTypeInformation) {
        super(customTypeInformation);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitCustom(this.typeInformation);
    }
}
