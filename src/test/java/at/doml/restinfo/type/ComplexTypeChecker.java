package at.doml.restinfo.type;

import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

final class ComplexTypeChecker extends TypeTreeChecker<ComplexType> {
    
    private final Map<String, TypeTreeChecker> fieldCheckers;
    
    ComplexTypeChecker(ComplexType expectedType, Map<String, TypeTreeChecker> fieldCheckers) {
        super(expectedType);
        this.fieldCheckers = Collections.unmodifiableMap(fieldCheckers);
    }
    
    @Override
    void additionalAssertions(ComplexType expectedType, ComplexType actualType) {
        assertEquals("actual type does not have expected number of fields",
                this.fieldCheckers.size(), actualType.fields.size());
        this.fieldCheckers.forEach((fieldName, fieldTypeChecker) -> {
            assertTrue("actual type is missing field: \"" + fieldName + '"', actualType.fields.containsKey(fieldName));
            fieldTypeChecker.assertType(actualType.fields.get(fieldName));
        });
    }
}
