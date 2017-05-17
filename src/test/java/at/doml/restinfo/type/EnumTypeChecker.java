package at.doml.restinfo.type;

import static org.junit.Assert.assertArrayEquals;

final class EnumTypeChecker extends TypeTreeChecker<EnumType> {

    EnumTypeChecker(EnumType expectedType) {
        super(expectedType);
    }

    @Override
    void additionalAssertions(EnumType expectedType, EnumType actualType) {
        assertArrayEquals("unexpected enum constants", expectedType.constants, actualType.constants);
    }
}
