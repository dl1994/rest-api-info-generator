package at.doml.restinfo.type;

import org.junit.Test;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public final class TypeInformationTest {

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
    public void multiDimensionalArraysShouldHaveSameRootType() {
        this.typesToTest.put(int[].class, "int");
        this.typesToTest.put(int[][].class, "int");
        this.typesToTest.put(int[][][].class, "int");
        this.typesToTest.put(int[][][][].class, "int");

        this.testTypes((expectedTypeString, ti) -> {
            assertType(expectedTypeString, ti);
            assertThatThereAreNoTypeParameters(ti);
        });
    }

    @Test
    public void typeWithSingleTypeParameterShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public List<String> test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.List",
                        typeInformationFor("java.lang.String")
                ),
                typeInformation
        );
    }

    @Test
    public void typeWithSingleNestedTypeParameterShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public List<Set<Collection<String>>> test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.List",
                        typeInformationFor(
                                "java.util.Set",
                                typeInformationFor(
                                        "java.util.Collection",
                                        typeInformationFor("java.lang.String")
                                )
                        )
                ),
                typeInformation
        );
    }

    @Test
    public void typeWithMultipleTypeParametersShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Map<String, Integer> test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.Map",
                        typeInformationFor("java.lang.String"),
                        typeInformationFor("java.lang.Integer")
                ),
                typeInformation
        );
    }

    @Test
    public void typeWithMultipleNestedTypeParametersShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Map<Long, Map<String, Map<Map<Character, Short>, List<Boolean>>>> test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.Map",
                        typeInformationFor("java.lang.Long"),
                        typeInformationFor(
                                "java.util.Map",
                                typeInformationFor("java.lang.String"),
                                typeInformationFor(
                                        "java.util.Map",
                                        typeInformationFor(
                                                "java.util.Map",
                                                typeInformationFor("java.lang.Character"),
                                                typeInformationFor("java.lang.Short")
                                        ),
                                        typeInformationFor(
                                                "java.util.List",
                                                typeInformationFor("java.lang.Boolean")
                                        )
                                )
                        )
                ),
                typeInformation
        );
    }

    @Test
    public void arrayTypesWithSingleTypeParameterShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public List<String>[] test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.List", 1,
                        typeInformationFor("java.lang.String")
                ),
                typeInformation
        );

        typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Set<Integer>[][][] test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.Set", 3,
                        typeInformationFor("java.lang.Integer")
                ),
                typeInformation
        );
    }

    @Test
    public void typesWithSingleArrayTypeParameterShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public List<String[]> test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.List",
                        typeInformationFor("java.lang.String", 1)
                ),
                typeInformation
        );

        typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Set<Integer[][][]> test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.Set",
                        typeInformationFor("java.lang.Integer", 3)
                ),
                typeInformation
        );
    }

    @Test
    public void arrayTypesWithMultipleNestedArrayTypeParametersShouldHaveCorrectHierarchy() {
        TypeInformation typeInformation = typeInformationFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Map<Long[], Map<Map<Character[], Short>[][][], boolean[][]>>[][][][][] test;
        });

        assertTypeParameterHierarchy(
                typeInformationFor(
                        "java.util.Map", 5,
                        typeInformationFor("java.lang.Long", 1),
                        typeInformationFor(
                                "java.util.Map",
                                typeInformationFor(
                                        "java.util.Map", 3,
                                        typeInformationFor("java.lang.Character", 1),
                                        typeInformationFor("java.lang.Short")
                                ),
                                typeInformationFor("boolean", 2)
                        )
                ),
                typeInformation
        );
    }

    @Test
    public void typeInformationShouldHaveCorrectToStringImplementation() throws NoSuchFieldException {
        Object object = new Object() {
            @SuppressWarnings("unused")
            public Map<Long, Map<String, Map<Map<Character, Short>, List<Boolean>>>> test;
        };
        TypeInformation typeInformation = typeInformationFromTestObject(object);
        String expectedString = object.getClass().getField("test").getGenericType().getTypeName();

        assertEquals("TypeInformation has incorrect toString method", expectedString, typeInformation.toString());
    }

    //
    // HELPER METHODS
    //
    private static TypeInformation typeInformationFor(Class<?> clazz) {
        return new TypeInformation(clazz);
    }

    private static TypeInformation typeInformationFor(String type, TypeInformation... typeParameters) {
        return typeInformationFor(type, 0, typeParameters);
    }

    private static TypeInformation typeInformationFor(String type, int arrayDimension,
                                                      TypeInformation... typeParameters) {
        return new TypeInformation(type, typeParameters, arrayDimension);
    }

    private static TypeInformation typeInformationFromTestObject(Object object) {
        try {
            return new TypeInformation(
                    object.getClass()
                            .getField("test")
                            .getGenericType()
            );
        } catch (NoSuchFieldException e) {
            fail("provided object has no field named \"test\" from which to fetch type information");
            throw new RuntimeException(e); // to make compiler happy
        }
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
        assertEquals("type is incorrect", expectedTypeString, ti.getTypeName());
    }

    private static void assertTypeParameterHierarchy(TypeInformation expectedHierarchy,
                                                     TypeInformation actualHierarchy) {
        TypeInformation[] actualTypeParameters = actualHierarchy.getTypeParameters();
        TypeInformation[] expectedTypeParameters = expectedHierarchy.getTypeParameters();

        assertType(expectedHierarchy.getTypeName(), actualHierarchy);
        assertEquals("invalid number of type parameters for " + actualHierarchy.getTypeName(),
                expectedTypeParameters.length, actualTypeParameters.length);
        assertThatTypeIsArrayWithDimension(expectedHierarchy.getArrayDimension(), actualHierarchy);

        for (int i = 0; i < expectedTypeParameters.length; i++) {
            assertTypeParameterHierarchy(expectedTypeParameters[i], actualTypeParameters[i]);
        }
    }

    private static void assertThatTypeIsArrayWithDimension(int expectedDimension, TypeInformation ti) {
        assertEquals(ti.getTypeName() + " should be an array with correct dimension",
                expectedDimension, ti.getArrayDimension());
    }

    private static void assertThatTypeIsNotFlaggedAsAnArray(TypeInformation ti) {
        assertFalse(ti.getTypeName() + " is not expected to be an array", ti.isArray());
    }

    private static void assertThatThereAreNoTypeParameters(TypeInformation ti) {
        assertEquals("no type parameters are expected for type " + ti.getTypeName(), 0, ti.getTypeParameters().length);
    }
}
