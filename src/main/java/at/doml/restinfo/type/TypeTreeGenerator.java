package at.doml.restinfo.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Class used to generate type tree which can be visited by {@link TypeVisitor} objects. The tree is composed of tokens
 * which represent certain types, such as collections, arrays, maps and so on. List of types generated by this generator
 * is same as the list of types visitable by <code>TypeVisitor</code>. The generator is configured during construction,
 * except for registration of custom types which can be done after generator object is created. By default, complex
 * types will only consist of their public fields and when an unknown type is encountered, an exception will be thrown.
 * Custom types can by registered and unregistered from the generator object by invoking
 * {@link TypeTreeGenerator#registerCustomType(Type)} and {@link TypeTreeGenerator#unregisterCustomType(Type)} methods.
 * The tree is generated by invoking the {@link TypeTreeGenerator#generateTree(Type)} method, passing it the type for
 * which to generate the tree. There is also an overloaded method {@link TypeTreeGenerator#generateTree(Map)} which
 * which takes a map of types as an argument. The map is used to generate a complex type whose field names are map keys
 * and field types are map values, with exception of complex types as their own fields are used in the root type.<br/>
 * <br/>
 * Types which have generic types parameters are also handled by the generator, provided that sufficient information
 * about type parameters is available for the provided type.<br/>
 * <br/>
 * Concurred invocation of {@link TypeTreeGenerator#generateTree(Type)} and {@link TypeTreeGenerator#generateTree(Map)}
 * is thread-safe, as long as no custom types are registered or unregistered concurrently during tree generation.<br/>
 * <br/>
 * For example, the following code is thread-safe:<br/>
 * <br/>
 * <code>Map&lt;String, Type&gt; map = ...;<br/>
 * Class&lt;?&gt;[] types = new Class[] { ... };<br/>
 * TypeTreeGenerator generator = new TypeTreeGenerator();<br/>
 * <br/>
 * for (Class&lt;?&gt; type : types) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;generator.registerCustomType(type);<br/>
 * }<br/>
 * <br/>
 * for (Class&lt;?&gt; type : types) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;new Thread(() -&gt; {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;generator.generateTree(map);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;generator.generateTree(type);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}).start();<br/>
 * <br/>
 * }</code><br/>
 * <br/>
 * While the following code is not thread-safe:<br/>
 * <br/>
 * <code>Map&lt;String, Type&gt; map = ...;<br/>
 * Class&lt;?&gt;[] types = new Class[] { ... };<br/>
 * TypeTreeGenerator generator = new TypeTreeGenerator();<br/>
 * <br/>
 * for (Class&lt;?&gt; type : types) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;new Thread(() -&gt; {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;generator.generateTree(map);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;generator.generateTree(type);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;generator.registerCustomType(type);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}).start();<br/>
 * }</code>
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see VisitableType
 */
public final class TypeTreeGenerator {

    //
    // CONSTANTS
    //
    private static final String NOT_NULL = " must not be null";
    private static final String TYPE = "type";
    private static final String TYPES = TYPE + 's';
    private static final String TYPE_NOT_NULL = TYPE + NOT_NULL;
    private static final String TYPES_NOT_NULL = TYPES + NOT_NULL;
    private static final String TYPES_NOT_EMPTY = TYPES + " must not be empty";
    private static final String METHOD_FIELD_EXTRACTION_NOT_NULL = "methodFieldExtraction" + NOT_NULL;
    private static final String UNKNOWN_TYPE_HANDLING_NOT_NULL = "unknownTypeHandling" + NOT_NULL;
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

    /**
     * Constructs a <code>TypeTreeGenerator</code> object with default settings. By default, both getters and setters
     * will be used to extract fields from complex types. Also, unknown types will cause the generator to throw
     * {@link UnknownTypeException}.
     */
    public TypeTreeGenerator() {
        this(DEFAULT_METHOD_FIELD_EXTRACTION, DEFAULT_UNKNOWN_TYPE_HANDLING);
    }

    /**
     * Constructs a <code>TypeTreeGenerator</code> object with provided setting for field extraction and default
     * unknown type handling. By default, unknown types will cause the generator to throw {@link UnknownTypeException}.
     * For more info about available field extraction settings see {@link MethodFieldExtraction}.
     *
     * @param methodFieldExtraction field extraction setting to use in this object
     * @throws NullPointerException if provided parameter is <code>null</code>
     */
    public TypeTreeGenerator(MethodFieldExtraction methodFieldExtraction) {
        this(methodFieldExtraction, DEFAULT_UNKNOWN_TYPE_HANDLING);
    }

    /**
     * Constructs a <code>TypeTreeGenerator</code> object with provided setting for unknown type handling and default
     * field extraction setting. By default, both getters and setters will be used to extract fields from complex types.
     * For more info about available unknown type handling settings see {@link UnknownTypeHandling}.
     *
     * @param unknownTypeHandling unknown type handling setting to use in this object
     * @throws NullPointerException if provided parameter is <code>null</code>
     */
    public TypeTreeGenerator(UnknownTypeHandling unknownTypeHandling) {
        this(DEFAULT_METHOD_FIELD_EXTRACTION, unknownTypeHandling);
    }

    /**
     * Constructs a <code>TypeTreeGenerator</code> object with provided settings for field extraction and unknown type
     * handling. For more info about available field extraction settings see {@link MethodFieldExtraction}, and for more
     * info about available unknown type handling settings see {@link UnknownTypeHandling}.
     *
     * @param methodFieldExtraction field extraction setting to use in this object
     * @param unknownTypeHandling   unknown type handling setting to use in this object
     * @throws NullPointerException if any of provided parameters is <code>null</code>
     */
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

    private static Map<String, Type> requireTypesNonEmpty(Map<String, Type> types) {
        if (types.isEmpty()) {
            throw new IllegalArgumentException(TYPES_NOT_EMPTY);
        }

        return types;
    }

    private static ComplexType mapToComplexType(Map.Entry<String, VisitableType> entry) {
        String fieldName = entry.getKey();
        VisitableType fieldType = entry.getValue();

        if (fieldType instanceof ComplexType) {
            return (ComplexType) fieldType;
        }

        ComplexType complexType = new ComplexType();

        complexType.addField(fieldName, fieldType);

        return complexType;
    }

    private static ComplexType mergeComplexTypes(ComplexType left, ComplexType right) {
        left.fields.putAll(right.fields);
        return left;
    }

    //
    // UTIL CLASSES AND INTERFACES
    //
    private interface ClassFieldFetcher {
        boolean canFetchFrom(Method method);

        TypeInformation fetchField(Method method, Map<String, String> typeNameMappings);
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
        public TypeInformation fetchField(Method method, Map<String, String> typeNameMappings) {
            return new TypeInformation(method.getGenericReturnType(), typeNameMappings);
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
        public TypeInformation fetchField(Method method, Map<String, String> typeNameMappings) {
            return new TypeInformation(method.getGenericParameterTypes()[0], typeNameMappings);
        }
    }

    /**
     * An enumeration which defines available settings for field extraction from methods in {@link TypeTreeGenerator}.
     * <br/>
     * Available settings are as follows:<ul>
     * <li>{@link MethodFieldExtraction#NONE} - no methods will be used to extract class fields</li>
     * <li>{@link MethodFieldExtraction#EXTRACT_GETTERS} - getters will be used to extract class fields</li>
     * <li>{@link MethodFieldExtraction#EXTRACT_SETTERS} - setters will be used to extract class fields</li>
     * <li>{@link MethodFieldExtraction#EXTRACT_BOTH} - getters and setters will be used to extract class fields</li>
     * </ul>
     *
     * @author Domagoj Latečki
     * @version 1.0.0
     */
    public enum MethodFieldExtraction {
        /**
         * No field extraction from methods will be performed.
         */
        NONE(),
        /**
         * Additional fields will be extracted using getter methods (non-<code>void</code>, no argument methods which
         * start with 'get' or 'is'). Field name is generated by dropping 'get' or 'is' from beginning of the method
         * name and lowercasing the following character. Field type is same as method return type.
         */
        EXTRACT_GETTERS(GetterFieldFetcher.INSTANCE),
        /**
         * Additional fields will be extracted using setter methods (<code>void</code>, single argument methods which
         * start with 'set'). Field name is generated by dropping 'set' from beginning of the method name and
         * lowercasing the following character. Field type is same as argument type of the method.
         */
        EXTRACT_SETTERS(SetterFieldFetcher.INSTANCE),
        /**
         * Additional fields will be extracted using both getters (non-<code>void</code>, no argument methods which
         * start with 'get' or 'is') and setters (<code>void</code>, single argument methods which start with 'set').
         * When extracting fields from getters, field names will be generated by dropping 'get' or 'is' from beginning
         * of the method name and lowercasing the following character. Field type will be same as method return type. In
         * case of setters, field names will be generated by dropping 'set' from beginning of the method name and
         * lowercasing the following character. Field type will be same as argument type of the method.
         */
        EXTRACT_BOTH(GetterFieldFetcher.INSTANCE, SetterFieldFetcher.INSTANCE);

        private final ClassFieldFetcher[] fetchers;

        MethodFieldExtraction(ClassFieldFetcher... fetchers) {
            this.fetchers = fetchers;
        }
    }

    /**
     * An enumeration which defines available settings for unknown type handling in {@link TypeTreeGenerator}.<br/>
     * Available settings are as follows:<ul>
     * <li>{@link UnknownTypeHandling#THROW_EXCEPTION} - exception will be thrown when unknown type is encountered</li>
     * <li>{@link UnknownTypeHandling#HANDLE_AS_CUSTOM} - unknown types will generate same tokens as custom types</li>
     * <li>{@link UnknownTypeHandling#USE_SPECIAL_TOKEN} - special token will be used for unknown types</li>
     * </ul>
     *
     * @author Domagoj Latečki
     * @version 1.0.0
     */
    public enum UnknownTypeHandling {
        /**
         * Throws {@link UnknownTypeException} when unknown type is encountered. The thrown exception will contain the
         * name of the unknown type which can be fetched by calling {@link UnknownTypeException#getTypeName()}. The
         * exception will also wrap the exception which caused it to be thrown (cause will always be an instance of
         * {@link ClassNotFoundException}).
         */
        THROW_EXCEPTION((typeInformation, cause) -> {
            throw new UnknownTypeException(typeInformation.getTypeName(), cause);
        }),
        /**
         * Unknown types will be handled as custom types, so they will produce the same token type.
         */
        HANDLE_AS_CUSTOM((typeInformation, ignored) -> new CustomType(typeInformation)),
        /**
         * Unknown types will produce distinct token types to differ them from other types in the type tree.
         */
        USE_SPECIAL_TOKEN((typeInformation, ignored) -> new UnknownType(typeInformation));

        private final BiFunction<TypeInformation, ClassNotFoundException, VisitableType> handler;

        UnknownTypeHandling(BiFunction<TypeInformation, ClassNotFoundException, VisitableType> handler) {
            this.handler = handler;
        }
    }

    //
    // INSTANCE METHODS
    //

    /**
     * Registers a type which will produce custom type tokens. Any type can be registered as a custom type, except for
     * arrays and types defined in {@link SimpleType}. Custom types will always have priority over other type classes
     * when generating type tree. For example, registering <code>java.util.ArrayList</code> as a custom type will cause
     * the generator to generate custom type tokens for <code>ArrayList</code>s instead of collection tokens. When
     * checking if some type is a registered as a custom type, full type name is used. This means that you cannot use
     * sub-classing to register all custom types at once: each custom class must be registered separately.
     *
     * @param type custom type to register
     * @throws NullPointerException if provided parameter is <code>null</code>
     */
    public void registerCustomType(Type type) {
        this.customTypes.add(getTypeName(type));
    }

    /**
     * Unregisters provided custom type. If provided type was not already registered, this method will effectively do
     * nothing.
     *
     * @param type custom type to unregister
     * @throws NullPointerException if provided parameter is <code>null</code>
     */
    public void unregisterCustomType(Type type) {
        this.customTypes.remove(getTypeName(type));
    }

    /**
     * Generates a type tree from provided root type. The type tree will be traversable using classes which implement
     * {@link TypeVisitor} interface.<br/>
     * <br/>
     * The tree will consist of following nodes:<ul>
     * <li>simple types - see {@link SimpleType}</li>
     * <li>enum types - any Java <code>enum</code></li>
     * <li>array types - types which represent any Java array. Multi-dimensional arrays will be represented as nested
     * one-dimensional arrays</li>
     * <li>collection types - any class which implements <code>java.util.Collection</code> interface</li>
     * <li>map - any class which implements <code>java.util.Map</code> interface</li>
     * <li>custom - any type registered as custom type before type tree generation. See
     * {@link TypeTreeGenerator#registerCustomType(Type)}</li>
     * <li>complex - any type which is composed of other types. All classes which are not registered as custom types and
     * can be obtained by the class loader will be put into this category</li>
     * </ul>
     * Additionally, depending on the provided {@link UnknownTypeHandling} setting, unknown type tokens may be
     * generated. Unknown types are all types not covered by the above definitions (i.e. types which cannot be loaded by
     * the class loader and are not registered as custom types).<br/>
     * <br/>
     * Example of generating type tree for the following class:<br/>
     * <br/>
     * <code>class SomeClass {<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public int a;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public String b;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public List&lt;Integer[]&gt; c;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public Map&lt;String, List&lt;Boolean[]&gt;&gt; d;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public RegisteredCustomClass e;<br/>
     * }</code><br/>
     * <br/>
     * Generator construction and invocation:<br/>
     * <br/>
     * <code>TypeTreeGenerator generator = new TypeTreeGenerator();<br/>
     * generator.registerCustomType(RegisteredCustomClass.class);<br/>
     * VisitableType treeRoot = generator.generateTree(SomeClass.class);
     * </code><br/>
     * <br/>
     * Generated tree:<br/>
     * <br/>
     * <code>ComplexType<br/>
     * ├─ &quot;a&quot;: SimpleType.INT<br/>
     * ├─ &quot;b&quot;: SimpleType.STRING<br/>
     * ├─ &quot;c&quot;: CollectionType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ ArrayType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ SimpleType.BOXED_INT<br/>
     * ├─ &quot;d&quot;: MapType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ SimpleType.STRING<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ CollectionType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ ArrayType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ SimpleType.BOXED_BOOLEAN<br/>
     * └─ &quot;e&quot;: CustomType(RegisteredCustomClass)<br/>
     * </code><br/>
     * Note: this method can handle generic type parameters, provided that <code>type</code> contains sufficient type
     * parameter information.<br/>
     * <br/>
     * Thread safety note: calling this method concurrently from multiple threads is safe, as long as
     * {@link TypeTreeGenerator#registerCustomType(Type)} and {@link TypeTreeGenerator#unregisterCustomType(Type)}
     * methods are not called concurrently with this method. Additionally, this method can be safely called concurrently
     * with {@link TypeTreeGenerator#generateTree(Map)} method.
     *
     * @param type root type for which to generate the type tree
     * @return root node of the generated type tree
     * @throws NullPointerException if provided parameter is <code>null</code>
     * @throws UnknownTypeException if unknown type is encountered and unknown type handling setting is set to
     *                              {@link UnknownTypeHandling#THROW_EXCEPTION}
     */
    public VisitableType generateTree(Type type) {
        return this.generateTree(new TypeInformation(Objects.requireNonNull(type, TYPE_NOT_NULL)));
    }

    /**
     * Generates a type tree from provided (non-empty) map of types. The type tree will be traversable using classes
     * which implement {@link TypeVisitor} interface. The root type of the tree generated by this method will always be
     * a complex type with fields which correspond to the map elements. The keys in the map are used for field names,
     * and values are used for field types. The exception to this rule are complex types in the map: their fields will
     * be used instead of the complex type itself.<br/>
     * <br/>
     * The tree will consist of following nodes:<ul>
     * <li>simple types - see {@link SimpleType}</li>
     * <li>enum types - any Java <code>enum</code></li>
     * <li>array types - types which represent any Java array. Multi-dimensional arrays will be represented as nested
     * one-dimensional arrays</li>
     * <li>collection types - any class which implements <code>java.util.Collection</code> interface</li>
     * <li>map - any class which implements <code>java.util.Map</code> interface</li>
     * <li>custom - any type registered as custom type before type tree generation. See
     * {@link TypeTreeGenerator#registerCustomType(Type)}</li>
     * <li>complex - any type which is composed of other types. All classes which are not registered as custom types and
     * can be obtained by the class loader will be put into this category</li>
     * </ul>
     * Additionally, depending on the provided {@link UnknownTypeHandling} setting, unknown type tokens may be
     * generated. Unknown types are all types not covered by the above definitions (i.e. types which cannot be loaded by
     * the class loader and are not registered as custom types).<br/>
     * <br/>
     * Example of generating type tree for the following classes:<br/>
     * <br/>
     * <code>class SomeClass {<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public int a;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public String b;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public List&lt;Integer[]&gt; c;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public Map&lt;String, List&lt;Boolean[]&gt;&gt; d;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public RegisteredCustomClass e;<br/>
     * }<br/>
     * <br/>
     * class OtherClass {<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public String f;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public Long g;<br/>
     * }</code><br/>
     * <br/>
     * Map used for tree generation:<br/>
     * <br/>
     * <code>types = {<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&quot;first&quot; =&gt; SomeClass.class,<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&quot;second&quot; =&gt; OtherClass.class,<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&quot;h&quot; =&gt; Boolean.class,<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&quot;i&quot; =&gt; String.class,<br/>
     * }</code><br/>
     * <br/>
     * Generator construction and invocation:<br/>
     * <br/>
     * <code>TypeTreeGenerator generator = new TypeTreeGenerator();<br/>
     * generator.registerCustomType(RegisteredCustomClass.class);<br/>
     * VisitableType treeRoot = generator.generateTree(types);
     * </code><br/>
     * <br/>
     * Generated tree:<br/>
     * <br/>
     * <code>ComplexType<br/>
     * ├─ &quot;a&quot;: SimpleType.INT<br/>
     * ├─ &quot;b&quot;: SimpleType.STRING<br/>
     * ├─ &quot;c&quot;: CollectionType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ ArrayType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ SimpleType.BOXED_INT<br/>
     * ├─ &quot;d&quot;: MapType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ SimpleType.STRING<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ CollectionType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ ArrayType<br/>
     * │&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ SimpleType.BOXED_BOOLEAN<br/>
     * ├─ &quot;e&quot;: CustomType(RegisteredCustomClass)<br/>
     * ├─ &quot;f&quot;: SimpleType.STRING<br/>
     * ├─ &quot;g&quot;: SimpleType.BOXED_LONG<br/>
     * ├─ &quot;h&quot;: SimpleType.BOXED_BOOLEAN<br/>
     * └─ &quot;i&quot;: SimpleType.STRING<br/>
     * </code><br/>
     * Note: this method can handle generic type parameters, provided that <code>type</code> contains sufficient type
     * parameter information.<br/>
     * <br/>
     * Thread safety note: calling this method concurrently from multiple threads is safe, as long as
     * {@link TypeTreeGenerator#registerCustomType(Type)} and {@link TypeTreeGenerator#unregisterCustomType(Type)}
     * methods are not called concurrently with this method. Additionally, this method can be safely called concurrently
     * with {@link TypeTreeGenerator#generateTree(Type)} method.
     *
     * @param types map of type names and types which will be fields of the root complex type
     * @return root node of the generated type tree
     * @throws NullPointerException     if provided parameter is <code>null</code>
     * @throws IllegalArgumentException if provided parameter is an empty map
     * @throws UnknownTypeException     if unknown type is encountered and unknown type handling setting is set to
     *                                  {@link UnknownTypeHandling#THROW_EXCEPTION}
     */
    public VisitableType generateTree(Map<String, Type> types) {
        return requireTypesNonEmpty(Objects.requireNonNull(types, TYPES_NOT_NULL))
                .entrySet()
                .stream()
                .map(this::generateTree)
                .map(TypeTreeGenerator::mapToComplexType)
                .reduce(new ComplexType(), TypeTreeGenerator::mergeComplexTypes);
    }

    //
    // PRIVATE METHODS
    //
    private Map.Entry<String, VisitableType> generateTree(Map.Entry<String, Type> entry) {
        return new AbstractMap.SimpleEntry<>(entry.getKey(), this.generateTree(entry.getValue()));
    }

    private VisitableType generateTree(TypeInformation typeInformation) {
        if (typeInformation.isArray()) {
            return this.generateTreeForArray(typeInformation);
        }

        String type = typeInformation.getTypeName();
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
            return this.unknownTypeHandling.handler.apply(typeInformation, exception);
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
                                typeInformation.getTypeName(), typeInformation.getTypeParameters(),
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
        Map<String, String> typeNameMappings = createTypeNameMappings(clazz, typeInformation);

        this.addFieldsToComplexType(clazz, typeNameMappings, complexType);
        this.addFieldsFromMethodsToComplexType(clazz, typeNameMappings, complexType);

        return complexType;
    }

    private static Map<String, String> createTypeNameMappings(Class<?> clazz, TypeInformation typeInformation) {
        Map<String, String> typeNameMappings = new HashMap<>();
        TypeVariable<?>[] genericTypeParameters = clazz.getTypeParameters();
        TypeInformation[] actualTypeParameters = typeInformation.getTypeParameters();

        int limit = Math.min(genericTypeParameters.length, actualTypeParameters.length);
        for (int i = 0; i < limit; i++) {
            typeNameMappings.put(genericTypeParameters[i].toString(), actualTypeParameters[i].toString());
        }

        return typeNameMappings;
    }

    private void addFieldsToComplexType(Class<?> clazz, Map<String, String> typeNameMappings, ComplexType complexType) {
        Field[] publicFields = clazz.getFields();

        for (Field publicField : publicFields) {
            complexType.addField(publicField.getName(), this.generateTree(
                    new TypeInformation(publicField.getGenericType(), typeNameMappings)
            ));
        }
    }

    private void addFieldsFromMethodsToComplexType(Class<?> clazz, Map<String, String> typeNameMappings,
                                                   ComplexType complexType) {
        Method[] publicMethods = clazz.getMethods();

        for (Method publicMethod : publicMethods) {
            for (ClassFieldFetcher fetcher : this.methodFieldExtraction.fetchers) {
                if (fetcher.canFetchFrom(publicMethod)) {
                    complexType.addField(getFieldName(publicMethod.getName()), this.generateTree(
                            fetcher.fetchField(publicMethod, typeNameMappings)
                    ));
                }
            }
        }
    }
}
