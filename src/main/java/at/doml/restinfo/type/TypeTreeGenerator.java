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

public final class TypeTreeGenerator {
    
    //
    // CONSTANTS
    //
    private static final Mode DEFAULT_MODE = Mode.EXTRACT_BOTH;
    private static final UnknownTypeHandling DEFAULT_UNKNOWN_TYPE_HANDLING = UnknownTypeHandling.THROW_EXCEPTION;
    private static final Map<String, SimpleType> SIMPLE_TYPE_MAPPINGS = new HashMap<>();
    
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
    private final Mode mode;
    private final UnknownTypeHandling unknownTypeHandling;
    private final Set<String> customTypes = new HashSet<>();
    
    public TypeTreeGenerator() {
        this(DEFAULT_MODE, DEFAULT_UNKNOWN_TYPE_HANDLING);
    }
    
    public TypeTreeGenerator(Mode mode) {
        this(mode, DEFAULT_UNKNOWN_TYPE_HANDLING);
    }
    
    public TypeTreeGenerator(UnknownTypeHandling unknownTypeHandling) {
        this(DEFAULT_MODE, unknownTypeHandling);
    }
    
    public TypeTreeGenerator(Mode mode, UnknownTypeHandling unknownTypeHandling) {
        this.mode = mode;
        this.unknownTypeHandling = unknownTypeHandling;
    }
    
    //
    // HELPER METHODS
    //
    private static void addSimpleMapping(Type type, SimpleType simpleType) {
        SIMPLE_TYPE_MAPPINGS.put(type.getTypeName(), simpleType);
    }
    
    private static String getFieldName(String methodName) {
        String withoutGetterSetterPrefix = methodName.replaceAll("^(get|is|set)", "");
        
        if (withoutGetterSetterPrefix.length() > 1) {
            return Character.toLowerCase(withoutGetterSetterPrefix.charAt(0)) + withoutGetterSetterPrefix.substring(1);
        }
        
        return withoutGetterSetterPrefix.toLowerCase();
    }
    
    private static boolean isVoid(Method method) {
        Class<?> returnType = method.getReturnType();
        return Objects.equals(returnType, void.class) || Objects.equals(returnType, Void.class);
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
            return method.getParameters().length == 0 && !isVoid(method) && !Objects.equals(name, "getClass")
                    && (name.startsWith("get") || name.startsWith("is"));
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
            return method.getParameters().length == 1 && isVoid(method) && method.getName().startsWith("set");
        }
        
        @Override
        public TypeInformation fetchField(Method method, Map<String, String> typeParameterMappings) {
            return new TypeInformation(method.getGenericParameterTypes()[0].getTypeName(),
                    typeParameterMappings);
        }
    }
    
    public enum Mode {
        NONE(),
        EXTRACT_GETTERS(GetterFieldFetcher.INSTANCE),
        EXTRACT_SETTERS(SetterFieldFetcher.INSTANCE),
        EXTRACT_BOTH(GetterFieldFetcher.INSTANCE, SetterFieldFetcher.INSTANCE);
        
        private final ClassFieldFetcher[] fetchers;
        
        Mode(ClassFieldFetcher... fetchers) {
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
        this.customTypes.add(type.getTypeName());
    }
    
    public void removeCustomType(Type type) {
        this.customTypes.remove(type.getTypeName());
    }
    
    @SuppressWarnings("unchecked")
    public VisitableType generateTree(Type type) {
        return this.generateTree(new TypeInformation(type.getTypeName()));
    }
    
    //
    // PRIVATE METHODS
    //
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
    
    // TODO refactor this
    private VisitableType generateTreeForComplexClass(Class<?> clazz, TypeInformation typeInformation) {
        Field[] publicFields = clazz.getFields();
        Method[] publicMethods = clazz.getMethods();
        ComplexType complexType = new ComplexType();
        Map<String, String> typeParameterMappings = new HashMap<>();
        TypeVariable<?>[] genericTypeParameters = clazz.getTypeParameters();
        TypeInformation[] actualTypeParameters = typeInformation.getTypeParameters();
        
        int limit = Math.min(genericTypeParameters.length, actualTypeParameters.length);
        for (int i = 0; i < limit; i++) {
            typeParameterMappings.put(genericTypeParameters[i].toString(), actualTypeParameters[i].toString());
        }
        
        for (Field publicField : publicFields) {
            complexType.addField(publicField.getName(), this.generateTree(
                    new TypeInformation(publicField.getGenericType().getTypeName(), typeParameterMappings)
            ));
        }
        
        for (Method publicMethod : publicMethods) {
            for (ClassFieldFetcher fetcher : this.mode.fetchers) {
                if (fetcher.canFetchFrom(publicMethod)) {
                    complexType.addField(getFieldName(publicMethod.getName()), this.generateTree(
                            fetcher.fetchField(publicMethod, typeParameterMappings)
                    ));
                }
            }
        }
        
        return complexType;
    }
    
    @SuppressWarnings("unchecked")
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
        
        Class<?> clazz;
        
        try {
            clazz = Class.forName(type);
        } catch (ClassNotFoundException exception) {
            return this.unknownTypeHandling.handler.apply(typeInformation, new UnknownTypeException(type, exception));
        }
        
        if (clazz.isEnum()) {
            return new EnumType(((Class<Enum<?>>) clazz).getEnumConstants());
        }
        
        if (Collection.class.isAssignableFrom(clazz)) {
            return this.generateTreeForCollection(typeInformation);
        }
        
        if (Map.class.isAssignableFrom(clazz)) {
            return this.generateTreeForMap(typeInformation);
        }
        
        return this.generateTreeForComplexClass(clazz, typeInformation);
    }
}
