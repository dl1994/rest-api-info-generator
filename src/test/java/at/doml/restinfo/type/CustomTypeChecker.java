package at.doml.restinfo.type;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

final class CustomTypeChecker extends TypeTreeChecker<CustomType> {
    
    CustomTypeChecker(CustomType expectedType) {
        super(expectedType);
    }
    
    @Override
    void additionalAssertions(CustomType expectedType, CustomType actualType) {
        assertNotNull("custom class must not be null", actualType);
        assertSame("custom class is incorrect", expectedType.customClass, actualType.customClass);
    }
}
