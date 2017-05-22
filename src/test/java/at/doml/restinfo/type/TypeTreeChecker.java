package at.doml.restinfo.type;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

abstract class TypeTreeChecker<T extends VisitableType> {

    private final T expectedType;

    TypeTreeChecker(T expectedType) {
        this.expectedType = expectedType;
    }

    @SuppressWarnings("unchecked")
    void assertType(VisitableType actualType) {
        assertNotNull("actual type is null", actualType);
        assertSame("type classes are not same", this.expectedType.getClass(), actualType.getClass());

        this.additionalAssertions(this.expectedType, (T) actualType);
    }

    abstract void additionalAssertions(T expectedType, T actualType);
}
