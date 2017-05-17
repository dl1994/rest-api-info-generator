package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import static at.doml.restinfo.util.Util.METHOD_FIELD_EXTRACTION_NOT_NULL;
import static at.doml.restinfo.util.Util.TYPE_NOT_NULL;
import static at.doml.restinfo.util.Util.UNKNOWN_TYPE_HANDLING_NOT_NULL;

public final class TypeTreeGenerator {

    //
    // CONSTANTS
    //
    private static final MethodFieldExtraction DEFAULT_METHOD_FIELD_EXTRACTION = MethodFieldExtraction.EXTRACT_BOTH;
    private static final UnknownTypeHandling DEFAULT_UNKNOWN_TYPE_HANDLING = UnknownTypeHandling.THROW_EXCEPTION;
    private static final Map<String, SimpleType> SIMPLE_TYPE_MAPPINGS = new HashMap<>();
    private static final Pattern GETTER_SETTER_REMOVAL_PATTERN = Pattern.compile("^(get|is|set)");

    static {
        addSimpleMapping(byte.class, SimpleType.BYTE);
        addSimpleMapping(short.class, SimpleType.SHORT);
        addSimpleMapping(int.class, SimpleType.INT);
        addSimpleMapping(long.class, SimpleType.LONG);
        addSimpleMapping(float.class, SimpleType.FLOAT);
        addSimpleMapping(double.class, SimpleType.DOUBLE);
        addSimpleMapping(char.class, SimpleType.CHAR);
        addSimpleMapping(boolean.class, SimpleType.BOOLEAN);
        addSimpleMapping(void.class, SimpleType.VOID);
        addSimpleMapping(Byte.class, SimpleType.BOXED_BYTE);
        addSimpleMapping(Short.class, SimpleType.BOXED_SHORT);
        addSimpleMapping(Integer.class, SimpleType.BOXED_INT);
        addSimpleMapping(BigInteger.class, SimpleType.BIGINT);
        addSimpleMapping(Long.class, SimpleType.BOXED_LONG);
        addSimpleMapping(Float.class, SimpleType.BOXED_FLOAT);
        addSimpleMapping(Double.class, SimpleType.BOXED_DOUBLE);
        addSimpleMapping(BigDecimal.class, SimpleType.DECIMAL);
        addSimpleMapping(Character.class, SimpleType.BOXED_CHAR);
        addSimpleMapping(Boolean.class, SimpleType.BOXED_BOOLEAN);
        addSimpleMapping(Void.class, SimpleType.BOXED_VOID);
        addSimpleMapping(String.class, SimpleType.STRING);
        addSimpleMapping(Object.class, SimpleType.OBJECT);
    }

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    private final MethodFieldExtraction methodFieldExtraction;
    private final UnknownTypeHandling unknownTypeHandling;
    private final Set<String> customTypes = new HashSet<>();

    public TypeTreeGenerator() {
        this(DEFAULT_METHOD_FIELD_EXTRACTION, DEFAULT_UNKNOWN_TYPE_HANDLING);
    }

    public TypeTreeGenerator(MethodFieldExtraction methodFieldExtraction) {
        this(methodFieldExtraction, DEFAULT_UNKNOWN_TYPE_HANDLING);
    }

    public TypeTreeGenerator(UnknownTypeHandling unknownTypeHandling) {
        this(DEFAULT_METHOD_FIELD_EXTRACTION, unknownTypeHandling);
    }

    public TypeTreeGenerator(MethodFieldExtraction methodFieldExtraction, UnknownTypeHandling unknownTypeHandling) {
        this.methodFieldExtraction = Objects.requireNonNull(methodFieldExtraction, METHOD_FIELD_EXTRACTION_NOT_NULL);
        this.unknownTypeHandling = Objects.requireNonNull(unknownTypeHandling, UNKNOWN_TYPE_HANDLING_NOT_NULL);
    }

    //
    // HELPER METHODS
    //
    private static void addSimpleMapping(Type type, SimpleType simpleType) {
        SIMPLE_TYPE_MAPPINGS.put(type.getTypeName(), simpleType);
    }

    private static String getFieldName(String methodName) {
        String withoutGetterSetterPrefix = GETTER_SETTER_REMOVAL_PATTERN.matcher(methodName).replaceAll("");

        if (withoutGetterSetterPrefix.length() > 1) {
            return Character.toLowerCase(withoutGetterSetterPrefix.charAt(0)) + withoutGetterSetterPrefix.substring(1);
        }

        return withoutGetterSetterPrefix.toLowerCase();
    }

    private static boolean isVoid(Method method) {
        Class<?> returnType = method.getReturnType();
        return Objects.equals(returnType, void.class) || Objects.equals(returnType, Void.class);
    }

    private static String getTypeName(Type type) {
        return Objects.requireNonNull(type, TYPE_NOT_NULL).getTypeName();
    }

    //
    // UTIL CLASSES
    //
    private interface ClassFieldFetcher {
        boolean canFetchFrom(Method method);

        TypeInformation fetchField(Method method, Map<String, String> typeParameterMappings);
    }

    private static class GetterFieldFetcher implements ClassFieldFetcher {

        private static final ClassFieldFetcher INSTANCE = new GetterFieldFetcher();

        @Override
        public boolean canFetchFrom(Method method) {
            String name = method.getName();
            return hasNoParameters(method) && !isVoid(method) && hasGetterName(name);
        }

        private static boolean hasNoParameters(Method method) {
            return method.getParameters().length == 0;
        }

        private static boolean hasGetterName(String methodName) {
            return !Objects.equals(methodName, "getClass") &&
                    (methodName.startsWith("get") || methodName.startsWith("is"));
        }

        @Override
        public TypeInformation fetchField(Method method, Map<String, String> typeParameterMappings) {
            return new TypeInformation(method.getGenericReturnType().getTypeName(), typeParameterMappings);
        }
    }

    private static class SetterFieldFetcher implements ClassFieldFetcher {

        private static final ClassFieldFetcher INSTANCE = new SetterFieldFetcher();

        @Override
        public boolean canFetchFrom(Method method) {
            return hasOneParameter(method) && isVoid(method) && method.getName().startsWith("set");
        }

        private static boolean hasOneParameter(Method method) {
            return method.getParameters().length == 1;
        }

        @Override
        public TypeInformation fetchField(Method method, Map<String, String> typeParameterMappings) {
            return new TypeInformation(method.getGenericParameterTypes()[0].getTypeName(),
                    typeParameterMappings);
        }
    }

    public enum MethodFieldExtraction {
        NONE(),
        EXTRACT_GETTERS(GetterFieldFetcher.INSTANCE),
        EXTRACT_SETTERS(SetterFieldFetcher.INSTANCE),
        EXTRACT_BOTH(GetterFieldFetcher.INSTANCE, SetterFieldFetcher.INSTANCE);

        private final ClassFieldFetcher[] fetchers;

        MethodFieldExtraction(ClassFieldFetcher... fetchers) {
            this.fetchers = fetchers;
        }
    }

    public enum UnknownTypeHandling {
        THROW_EXCEPTION((ignored, exception) -> { throw exception; }),
        HANDLE_AS_CUSTOM((typeInformation, ignored) -> new CustomType(typeInformation)),
        USE_SPECIAL_TOKEN((typeInformation, ignored) -> new UnknownType(typeInformation));

        private final BiFunction<TypeInformation, UnknownTypeException, VisitableType> handler;

        UnknownTypeHandling(BiFunction<TypeInformation, UnknownTypeException, VisitableType> handler) {
            this.handler = handler;
        }
    }

    //
    // INSTANCE METHODS
    //
    public void registerCustomType(Type type) {
        this.customTypes.add(getTypeName(type));
    }

    public void removeCustomType(Type type) {
        this.customTypes.remove(getTypeName(type));
    }

    public VisitableType generateTree(Type type) {
        return this.generateTree(new TypeInformation(getTypeName(type)));
    }

    //
    // PRIVATE METHODS
    //
    private VisitableType generateTree(TypeInformation typeInformation) {
        if (typeInformation.isArray()) {
            return this.generateTreeForArray(typeInformation);
        }

        String type = typeInformation.getType();
        SimpleType simpleType = SIMPLE_TYPE_MAPPINGS.get(type);

        if (simpleType != null) {
            return simpleType;
        }

        if (this.customTypes.contains(type)) {
            return new CustomType(typeInformation);
        }

        try {
            return this.handleClass(Class.forName(type), typeInformation);
        } catch (ClassNotFoundException exception) {
            return this.unknownTypeHandling.handler.apply(typeInformation, new UnknownTypeException(type, exception));
        }
    }

    private VisitableType handleClass(Class<?> clazz, TypeInformation typeInformation) {
        if (clazz.isEnum()) {
            return new EnumType((Enum<?>[]) clazz.getEnumConstants());
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            return this.generateTreeForCollection(typeInformation);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return this.generateTreeForMap(typeInformation);
        }

        return this.generateTreeForComplexClass(clazz, typeInformation);
    }

    private VisitableType generateTreeForArray(TypeInformation typeInformation) {
        return new ArrayType(
                this.generateTree(
                        new TypeInformation(
                                typeInformation.getType(), typeInformation.getTypeParameters(),
                                typeInformation.getArrayDimension() - 1
                        )
                )
        );
    }

    private VisitableType generateTreeForCollection(TypeInformation typeInformation) {
        return new CollectionType(this.generateTree(typeInformation.getTypeParameters()[0]));
    }

    private VisitableType generateTreeForMap(TypeInformation typeInformation) {
        TypeInformation[] typeParameters = typeInformation.getTypeParameters();
        TypeInformation keyType = typeParameters[0];
        TypeInformation valueType = typeParameters[1];

        return new MapType(
                this.generateTree(keyType),
                this.generateTree(valueType)
        );
    }

    private VisitableType generateTreeForComplexClass(Class<?> clazz, TypeInformation typeInformation) {
        ComplexType complexType = new ComplexType();
        Map<String, String> typeParameterMappings = createTypeParameterMappings(clazz, typeInformation);

        this.addFieldsToComplexType(clazz, typeParameterMappings, complexType);
        this.addFieldsFromMethodsToComplexType(clazz, typeParameterMappings, complexType);

        return complexType;
    }

    private static Map<String, String> createTypeParameterMappings(Class<?> clazz, TypeInformation typeInformation) {
        Map<String, String> typeParameterMappings = new HashMap<>();
        TypeVariable<?>[] genericTypeParameters = clazz.getTypeParameters();
        TypeInformation[] actualTypeParameters = typeInformation.getTypeParameters();

        int limit = Math.min(genericTypeParameters.length, actualTypeParameters.length);
        for (int i = 0; i < limit; i++) {
            typeParameterMappings.put(genericTypeParameters[i].toString(), actualTypeParameters[i].toString());
        }

        return typeParameterMappings;
    }

    private void addFieldsToComplexType(Class<?> clazz, Map<String, String> typeParameterMappings,
                                        ComplexType complexType) {
        Field[] publicFields = clazz.getFields();

        for (Field publicField : publicFields) {
            complexType.addField(publicField.getName(), this.generateTree(
                    new TypeInformation(publicField.getGenericType().getTypeName(), typeParameterMappings)
            ));
        }
    }

    private void addFieldsFromMethodsToComplexType(Class<?> clazz, Map<String, String> typeParameterMappings,
                                                   ComplexType complexType) {
        Method[] publicMethods = clazz.getMethods();

        for (Method publicMethod : publicMethods) {
            for (ClassFieldFetcher fetcher : this.methodFieldExtraction.fetchers) {
                if (fetcher.canFetchFrom(publicMethod)) {
                    complexType.addField(getFieldName(publicMethod.getName()), this.generateTree(
                            fetcher.fetchField(publicMethod, typeParameterMappings)
                    ));
                }
            }
        }
    }
}
