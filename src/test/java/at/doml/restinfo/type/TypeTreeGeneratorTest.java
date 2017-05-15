package at.doml.restinfo.type;

import org.junit.Test;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import static org.junit.Assert.fail;

public final class TypeTreeGeneratorTest {
    
    private TypeTreeGenerator generator;
    
    //
    // TESTS
    //
    @Test
    public void arrayTypeShouldHaveCorrectElementTypeInTypeTree() {
        this.initGenerator();
        this.treeFor(int[].class).assertStructure(array(simple(SimpleType.INT)));
    }
    
    @Test
    public void arrayTypeNestingShouldGenerateCorrectTypeTree() {
        this.initGenerator();
        this.treeFor(int[][][].class).assertStructure(array(array(simple(SimpleType.INT))));
    }
    
    @Test
    public void collectionTypeShouldHaveCorrectElementTypeInTypeTree() {
        this.initGenerator();
        this.treeFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public List<String> test;
        }).assertStructure(collection(simple(SimpleType.STRING)));
    }
    
    @Test
    public void collectionTypeNestingShouldGenerateCorrectTypeTree() {
        this.initGenerator();
        this.treeFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public List<Collection<Set<String>>> test;
        }).assertStructure(collection(collection(collection(simple(SimpleType.STRING)))));
    }
    
    @Test
    public void complexTypeShouldHaveCorrectFieldTypesAndNamesInTypeTree() {
        this.initGenerator();
        this.treeFor(new Object() {
            @SuppressWarnings("unused")
            public int field1;
            @SuppressWarnings("unused")
            public String field2;
            @SuppressWarnings("unused")
            public Object field3;
        }.getClass()).assertStructure(
                complex(
                        field("field1", simple(SimpleType.INT)),
                        field("field2", simple(SimpleType.STRING)),
                        field("field3", simple(SimpleType.OBJECT))
                )
        );
    }
    
    private static final Object WITH_GETTERS_AND_SETTERS = new Object() {
        @SuppressWarnings("unused")
        public boolean shared;
        
        @SuppressWarnings("unused")
        public int getField1() {
            return 0;
        }
        
        @SuppressWarnings("unused")
        public String getField2() {
            return "";
        }
        
        @SuppressWarnings("unused")
        public byte getWrongGetterSignature(byte b) { return b; }
        
        @SuppressWarnings("unused")
        public void setField3(Object o) {}
        
        @SuppressWarnings("unused")
        public void setField4(Long l) {}
        
        @SuppressWarnings("unused")
        public Short setWrongSetterSignature(Short s) { return s; }
    };
    
    @Test
    public void complexTypeShouldHaveCorrectFieldTypesAndNamesInTypeTreeWhenUsingGetters() {
        this.initGenerator(TypeTreeGenerator.Mode.EXTRACT_GETTERS);
        this.treeFor(WITH_GETTERS_AND_SETTERS.getClass()).assertStructure(
                complex(
                        field("shared", simple(SimpleType.BOOLEAN)),
                        field("field1", simple(SimpleType.INT)),
                        field("field2", simple(SimpleType.STRING))
                )
        );
    }
    
    @Test
    public void complexTypeShouldHaveCorrectFieldTypesAndNamesInTypeTreeWhenUsingSetters() {
        this.initGenerator(TypeTreeGenerator.Mode.EXTRACT_SETTERS);
        this.treeFor(WITH_GETTERS_AND_SETTERS.getClass()).assertStructure(
                complex(
                        field("shared", simple(SimpleType.BOOLEAN)),
                        field("field3", simple(SimpleType.OBJECT)),
                        field("field4", simple(SimpleType.BOXED_LONG))
                )
        );
    }
    
    @Test
    public void complexTypeNestingShouldGenerateCorrectTypeTree() {
        this.initGenerator();
        final class TestType {
            @SuppressWarnings("unused")
            public int nestedField1;
            @SuppressWarnings("unused")
            public boolean nestedField2;
        }
        
        this.treeFor(new Object() {
            @SuppressWarnings("unused")
            public TestType field1;
            @SuppressWarnings("unused")
            public Integer field2;
        }.getClass()).assertStructure(
                complex(
                        field("field1", complex(
                                field("nestedField1", simple(SimpleType.INT)),
                                field("nestedField2", simple(SimpleType.BOOLEAN))
                        )),
                        field("field2", simple(SimpleType.BOXED_INT))
                )
        );
    }
    
    @Test
    public void customTypeShouldHaveCorrectTypeInformationInTypeTree() {
        final class CustomClass {}
        this.initGenerator(CustomClass.class);
        this.treeFor(CustomClass.class).assertStructure(custom(CustomClass.class));
    }
    
    @Test
    public void enumTypeShouldHaveCorrectConstantsInTypeTree() {
        this.initGenerator();
        this.treeFor(TestEnum.class).assertStructure(enumConstants(TestEnum.values()));
    }
    
    @Test
    public void mapTypeShouldHaveCorrectKeyAndValueTypesInTypeTree() {
        this.initGenerator();
        this.treeFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Map<String, Integer> test;
        }).assertStructure(map(simple(SimpleType.STRING), simple(SimpleType.BOXED_INT)));
    }
    
    @Test
    public void mapTypeNestingShouldGenerateCorrectTypeTree() {
        this.initGenerator();
        this.treeFromTestObject(new Object() {
            @SuppressWarnings("unused")
            public Map<Map<Boolean, String>, Map<Integer, Long>> test;
        }).assertStructure(
                map(
                        map(simple(SimpleType.BOXED_BOOLEAN), simple(SimpleType.STRING)),
                        map(simple(SimpleType.BOXED_INT), simple(SimpleType.BOXED_LONG))
                )
        );
    }
    
    @Test
    public void simpleTypesShouldHaveCorrectWritableTypeClassesInTypeTree() {
        this.initGenerator();
        this.treeFor(byte.class).assertStructure(simple(SimpleType.BYTE));
        this.treeFor(short.class).assertStructure(simple(SimpleType.SHORT));
        this.treeFor(int.class).assertStructure(simple(SimpleType.INT));
        this.treeFor(long.class).assertStructure(simple(SimpleType.LONG));
        this.treeFor(float.class).assertStructure(simple(SimpleType.FLOAT));
        this.treeFor(double.class).assertStructure(simple(SimpleType.DOUBLE));
        this.treeFor(char.class).assertStructure(simple(SimpleType.CHAR));
        this.treeFor(boolean.class).assertStructure(simple(SimpleType.BOOLEAN));
        this.treeFor(void.class).assertStructure(simple(SimpleType.VOID));
        this.treeFor(Byte.class).assertStructure(simple(SimpleType.BOXED_BYTE));
        this.treeFor(Short.class).assertStructure(simple(SimpleType.BOXED_SHORT));
        this.treeFor(Integer.class).assertStructure(simple(SimpleType.BOXED_INT));
        this.treeFor(Long.class).assertStructure(simple(SimpleType.BOXED_LONG));
        this.treeFor(BigInteger.class).assertStructure(simple(SimpleType.BIGINT));
        this.treeFor(Float.class).assertStructure(simple(SimpleType.BOXED_FLOAT));
        this.treeFor(Double.class).assertStructure(simple(SimpleType.BOXED_DOUBLE));
        this.treeFor(BigDecimal.class).assertStructure(simple(SimpleType.DECIMAL));
        this.treeFor(Character.class).assertStructure(simple(SimpleType.BOXED_CHAR));
        this.treeFor(String.class).assertStructure(simple(SimpleType.STRING));
        this.treeFor(Boolean.class).assertStructure(simple(SimpleType.BOXED_BOOLEAN));
        this.treeFor(Void.class).assertStructure(simple(SimpleType.BOXED_VOID));
        this.treeFor(Object.class).assertStructure(simple(SimpleType.OBJECT));
    }
    
    //
    // PRIVATE CLASSES
    //
    private static final class TypeTreeStub {
        private final VisitableType tree;
        
        private TypeTreeStub(VisitableType tree) {
            this.tree = tree;
        }
        
        private void assertStructure(TypeTreeChecker expectedTree) {
            expectedTree.assertType(this.tree);
        }
    }
    
    private enum TestEnum {
        @SuppressWarnings("unused")
        CONST_1,
        @SuppressWarnings("unused")
        CONST_2,
        @SuppressWarnings("unused")
        CONST_3
    }
    
    //
    // HELPER METHODS
    //
    private void initGenerator() {
        this.generator = new TypeTreeGenerator();
    }
    
    private void initGenerator(TypeTreeGenerator.Mode mode) {
        this.generator = new TypeTreeGenerator(mode);
    }
    
    private void initGenerator(Class<?>... customClasses) {
        this.generator = new TypeTreeGenerator();
        for (Class<?> customClass : customClasses) {
            this.generator.registerCustomClass(customClass);
        }
    }
    
    private TypeTreeStub treeFor(Type type) {
        return new TypeTreeStub(this.generator.generateTree(type));
    }
    
    private TypeTreeStub treeFromTestObject(Object object) {
        try {
            return this.treeFor(
                    object.getClass()
                            .getField("test")
                            .getGenericType()
            );
        } catch (NoSuchFieldException e) {
            fail("provided object has no field named \"test\" from which to fetch type information");
            throw new RuntimeException(e); // to make compiler happy
        }
    }
    
    private static TypeTreeChecker simple(SimpleType expectedType) {
        return new SimpleTypeChecker(expectedType);
    }
    
    @SafeVarargs
    private static TypeTreeChecker complex(Map.Entry<String, TypeTreeChecker>... fields) {
        Map<String, TypeTreeChecker> expectedFieldCheckers = new HashMap<>();
        
        for (Map.Entry<String, TypeTreeChecker> field : fields) {
            expectedFieldCheckers.put(field.getKey(), field.getValue());
        }
        
        return new ComplexTypeChecker(new ComplexType(), expectedFieldCheckers);
    }
    
    private static TypeTreeChecker custom(Class<?> customClass) {
        return new CustomTypeChecker(new CustomType(customClass));
    }
    
    private static Map.Entry<String, TypeTreeChecker> field(String name, TypeTreeChecker checker) {
        return new AbstractMap.SimpleEntry<>(name, checker);
    }
    
    private static TypeTreeChecker enumConstants(Enum[] expectedconstants) {
        return new EnumTypeChecker(new EnumType(expectedconstants));
    }
    
    private static TypeTreeChecker array(TypeTreeChecker expectedTypeTreeChecker) {
        return collectionOrArray(expectedTypeTreeChecker, ArrayType::new);
    }
    
    private static TypeTreeChecker collection(TypeTreeChecker expectedTypeTreeChecker) {
        return collectionOrArray(expectedTypeTreeChecker, CollectionType::new);
    }
    
    private static TypeTreeChecker map(TypeTreeChecker keyChecker, TypeTreeChecker valueChecker) {
        return new MapTypeChecker(new MapType(), keyChecker, valueChecker);
    }
    
    private static TypeTreeChecker collectionOrArray(TypeTreeChecker expectedTypeTreeChecker,
                                                     Supplier<CollectionOrArrayType> constructor) {
        return new CollectionOrArrayTypeChecker<>(constructor.get(), expectedTypeTreeChecker);
    }
}
