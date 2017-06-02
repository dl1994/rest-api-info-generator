package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class TypeTreeStub {

    private final VisitableType tree;

    public TypeTreeStub(VisitableType tree) {
        this.tree = tree;
    }

    public void assertStructure(TypeTreeChecker expectedTree) {
        expectedTree.assertType(this.tree);
    }

    private static final class None implements VisitableType {

        private static final VisitableType INSTANCE = new None();

        @Override
        public void accept(TypeVisitor typeVisitor) {
            // Not visited
        }
    }

    public static TypeTreeChecker simple(SimpleType expectedType) {
        return new SimpleTypeChecker(expectedType);
    }

    @SafeVarargs
    public static TypeTreeChecker complex(Map.Entry<String, TypeTreeChecker>... fields) {
        Map<String, TypeTreeChecker> expectedFieldCheckers = new HashMap<>();

        for (Map.Entry<String, TypeTreeChecker> field : fields) {
            expectedFieldCheckers.put(field.getKey(), field.getValue());
        }

        return new ComplexTypeChecker(new ComplexType(), expectedFieldCheckers);
    }

    public static TypeTreeChecker custom(String typeName) {
        return customOrUnknown(typeName, CustomType::new);
    }

    public static TypeTreeChecker unknown() {
        return customOrUnknown("unknown", UnknownType::new);
    }

    public static TypeTreeChecker customOrUnknown(String typeName,
                                                   Function<TypeInformation, CustomOrUnknownType> constructor) {
        return new CustomOrUnknownTypeChecker<>(constructor.apply(
                new TypeInformation(typeName, new TypeInformation[0], 0)
        ));
    }

    public static Map.Entry<String, TypeTreeChecker> field(String name, TypeTreeChecker checker) {
        return new AbstractMap.SimpleEntry<>(name, checker);
    }

    public static TypeTreeChecker enumConstants(Enum[] expectedConstants) {
        return new EnumTypeChecker(new EnumType(expectedConstants));
    }

    public static TypeTreeChecker array(TypeTreeChecker expectedTypeTreeChecker) {
        return collectionOrArray(expectedTypeTreeChecker, ArrayType::new);
    }

    public static TypeTreeChecker collection(TypeTreeChecker expectedTypeTreeChecker) {
        return collectionOrArray(expectedTypeTreeChecker, CollectionType::new);
    }

    public static TypeTreeChecker map(TypeTreeChecker keyChecker, TypeTreeChecker valueChecker) {
        return new MapTypeChecker(new MapType(None.INSTANCE, None.INSTANCE), keyChecker, valueChecker);
    }

    public static TypeTreeChecker collectionOrArray(TypeTreeChecker expectedTypeTreeChecker,
                                                     Function<VisitableType, CollectionOrArrayType> constructor) {
        return new CollectionOrArrayTypeChecker<>(constructor.apply(None.INSTANCE), expectedTypeTreeChecker);
    }
}
