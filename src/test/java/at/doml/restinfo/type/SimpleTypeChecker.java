package at.doml.restinfo.type;

import static org.junit.Assert.assertSame;

final class SimpleTypeChecker extends TypeTreeChecker<SimpleType> {

    SimpleTypeChecker(SimpleType expectedType) {
        super(expectedType);
    }

    @Override
    void additionalAssertions(SimpleType expectedType, SimpleType actualType) {
        assertSame("simple types are not same", expectedType, actualType);
    }
}
