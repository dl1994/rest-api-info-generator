package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

final class CustomOrUnknownTypeChecker<T extends CustomOrUnknownType> extends TypeTreeChecker<T> {

    CustomOrUnknownTypeChecker(T expectedType) {
        super(expectedType);
    }

    @Override
    void additionalAssertions(T expectedType, T actualType) {
        TypeInformation expectedTypeInformation = expectedType.typeInformation;
        TypeInformation actualTypeInformation = actualType.typeInformation;

        assertNotNull("custom type information must not be null", actualTypeInformation);
        assertEquals("custom type name is incorrect", expectedTypeInformation.getType(),
                actualTypeInformation.getType());
        assertEquals("custom array dimension is incorrect", expectedTypeInformation.getArrayDimension(),
                actualTypeInformation.getArrayDimension());
        assertEquals("custom type has incorrect number of type parameters",
                expectedTypeInformation.getTypeParameters().length, actualTypeInformation.getTypeParameters().length);
    }
}
