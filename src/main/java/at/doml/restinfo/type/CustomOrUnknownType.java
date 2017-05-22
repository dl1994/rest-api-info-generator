package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;

/**
 * Abstract class which represents custom or unknown type in type tree.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
abstract class CustomOrUnknownType implements VisitableType {

    /**
     * Objects which contains additional information about this type.
     */
    final TypeInformation typeInformation;

    /**
     * Constructs an object with specified type information.
     *
     * @param typeInformation additional type information about this type
     */
    CustomOrUnknownType(TypeInformation typeInformation) {
        this.typeInformation = typeInformation;
    }
}
