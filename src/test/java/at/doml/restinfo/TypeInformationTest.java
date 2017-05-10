package at.doml.restinfo;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TypeInformationTest {
    
    private final Map<Class<?>, String> typesToTest = new HashMap<>();
    
    //
    // TESTS
    //
    @Test
    public void primitiveTypesShouldNotHaveAnyTypeParameterAssociations() {
        this.typesToTest.put(byte.class, "byte");
        this.typesToTest.put(short.class, "short");
        this.typesToTest.put(int.class, "int");
        this.typesToTest.put(long.class, "long");
        this.typesToTest.put(float.class, "float");
        this.typesToTest.put(double.class, "double");
        this.typesToTest.put(char.class, "char");
        this.typesToTest.put(boolean.class, "boolean");
        this.typesToTest.put(void.class, "void");
        
        this.testTypes((expectedTypeString, ti) -> {
            assertType(expectedTypeString, ti);
            assertThatTypeIsNotFlaggedAsAnArray(ti);
            assertThatThereAreNoTypeParameters(ti);
        });
    }
    
    @Test
    public void referenceTypesWithNoTypeParametersShouldNotHaveAnyTypeParameterAssociations() {
        this.typesToTest.put(Byte.class, "java.lang.Byte");
        this.typesToTest.put(Short.class, "java.lang.Short");
        this.typesToTest.put(Integer.class, "java.lang.Integer");
        this.typesToTest.put(Long.class, "java.lang.Long");
        this.typesToTest.put(Float.class, "java.lang.Float");
        this.typesToTest.put(Double.class, "java.lang.Double");
        this.typesToTest.put(Character.class, "java.lang.Character");
        this.typesToTest.put(Boolean.class, "java.lang.Boolean");
        this.typesToTest.put(Void.class, "java.lang.Void");
        this.typesToTest.put(String.class, "java.lang.String");
        this.typesToTest.put(Object.class, "java.lang.Object");
        
        this.testTypes((expectedTypeString, ti) -> {
            assertType(expectedTypeString, ti);
            assertThatTypeIsNotFlaggedAsAnArray(ti);
            assertThatThereAreNoTypeParameters(ti);
        });
    }
    
    @Test
    public void primitiveArraysShouldHaveSameTypeAsPrimitiveType() {
        this.typesToTest.put(byte[].class, "byte");
        this.typesToTest.put(short[].class, "short");
        this.typesToTest.put(int[].class, "int");
        this.typesToTest.put(long[].class, "long");
        this.typesToTest.put(float[].class, "float");
        this.typesToTest.put(double[].class, "double");
        this.typesToTest.put(char[].class, "char");
        this.typesToTest.put(boolean[].class, "boolean");
        
        this.testTypes((expectedTypeString, ti) -> {
            assertType(expectedTypeString, ti);
            assertThatTypeIsArrayWithDimension(1, ti);
            assertThatThereAreNoTypeParameters(ti);
        });
    }
    
    @Test
    public void referenceArraysShouldHaveSameTypeAsReferenceType() {
        this.typesToTest.put(Byte[].class, "java.lang.Byte");
        this.typesToTest.put(Short[].class, "java.lang.Short");
        this.typesToTest.put(Integer[].class, "java.lang.Integer");
        this.typesToTest.put(Long[].class, "java.lang.Long");
        this.typesToTest.put(Float[].class, "java.lang.Float");
        this.typesToTest.put(Double[].class, "java.lang.Double");
        this.typesToTest.put(Character[].class, "java.lang.Character");
        this.typesToTest.put(Boolean[].class, "java.lang.Boolean");
        this.typesToTest.put(Void[].class, "java.lang.Void");
        this.typesToTest.put(String[].class, "java.lang.String");
        this.typesToTest.put(Object[].class, "java.lang.Object");
        
        this.testTypes((expectedTypeString, ti) -> {
            assertType(expectedTypeString, ti);
            assertThatTypeIsArrayWithDimension(1, ti);
            assertThatThereAreNoTypeParameters(ti);
        });
    }
    
    @Test
    public void multiDimensionalArraysShouldHaveCorrectDimensionInTypeInformation() {
        assertThatTypeIsArrayWithDimension(1, typeInformationFor(int[].class));
        assertThatTypeIsArrayWithDimension(2, typeInformationFor(int[][].class));
        assertThatTypeIsArrayWithDimension(3, typeInformationFor(int[][][].class));
        assertThatTypeIsArrayWithDimension(4, typeInformationFor(int[][][][].class));
    }
    
    @Test
    public void multiDimensionalArraysShoulHaveSameRootType() {
        this.typesToTest.put(int[].class, "int");
        this.typesToTest.put(int[][].class, "int");
        this.typesToTest.put(int[][][].class, "int");
        this.typesToTest.put(int[][][][].class, "int");
        
        this.testTypes((expectedTypeString, ti) -> {
            assertType(expectedTypeString, ti);
            assertThatThereAreNoTypeParameters(ti);
        });
    }
    
    // TODO: add tests for generics
    
    //
    // HELPER METHODS
    //
    private static TypeInformation typeInformationFor(Class<?> clazz) {
        return new TypeInformation(clazz.getTypeName());
    }
    
    private void testTypes(BiConsumer<String, TypeInformation> assertions) {
        this.typesToTest.forEach((clazz, expectedTypeString) -> assertions.accept(
                expectedTypeString, typeInformationFor(clazz)
        ));
    }
    
    //
    // ASSERTIONS
    //
    private static void assertType(String expectedTypeString, TypeInformation ti) {
        assertEquals("type is incorrect", expectedTypeString, ti.getType());
    }
    
    private static void assertThatTypeIsArrayWithDimension(int expectedDimension, TypeInformation ti) {
        assertEquals(ti.getType() + " should be an array with correct dimension",
                expectedDimension, ti.getArrayDimension());
    }
    
    private static void assertThatTypeIsNotFlaggedAsAnArray(TypeInformation ti) {
        assertFalse(ti.getType() + " is not expected to be an array", ti.isArray());
    }
    
    private static void assertThatThereAreNoTypeParameters(TypeInformation ti) {
        assertEquals("no type parameters are expected for type " + ti.getType(), 0, ti.getTypeParameters().length);
    }
}
