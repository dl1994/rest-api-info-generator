package at.doml.restinfo.type;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Class used to store and extract information about a type, including its type parameters and dimension of array for
 * array types. Type information is stored in a tree-like structure, with provided type being a root node of the tree
 * (and a leaf node if there are no type parameters) and type parameters being intermediate and leaf nodes. This class
 * has three public constructors: two which extract type information using the provided {@link Type} object, and
 * one which accepts custom type information.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 */
public final class TypeInformation {

    //
    // CONSTANTS
    //
    private static final String NOT_NULL = " must not be null";
    private static final String TYPE = "type";
    private static final String TYPE_NOT_NULL = TYPE + NOT_NULL;
    private static final String TYPE_PARAMETERS_NOT_NULL = "typeParameters" + NOT_NULL;
    private static final String TYPE_REGEX = "[^\\s<>\\[\\]]+";
    private static final String TYPE_REGEX_MUST_BE_VALID =
            TYPE + " must satisfy the following regular expression: " + TYPE_REGEX;
    private static final String ARRAY_DIMENSION_NON_NEGATIVE = "arrayDimension must not be negative";
    private static final String TYPE_PARAMETER_MAPPINGS_NOT_NULL = "typeParameterMappings" + NOT_NULL;
    private static final Pattern ARRAY_REMOVAL_PATTERN = Pattern.compile("(\\[])+$");
    private static final Pattern OPENING_DIAMOND_SPLIT_PATTERN = Pattern.compile("<", Pattern.LITERAL);
    private static final Pattern CLOSING_DIAMOND_REMOVAL_PATTERN = Pattern.compile(">(\\[])*$");
    private static final Predicate<String> TYPE_NAME_VALIDATOR = Pattern.compile(TYPE_REGEX).asPredicate();

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    private final int arrayDimension;
    private final String typeName;
    private final TypeInformation[] typeParameters;

    /**
     * Constructs an object which will contain information about provided <code>type</code>, including its type
     * parameters. All type names will be mapped using the provided <code>typeNameMappings</code> map, including
     * the name of provided <code>type</code>. If there is no entry in the map for some type name, non-mapped name will
     * be used instead.<br/>
     * <br/>
     * For example, if following input is provided:<br/>
     * <br/>
     * <code>type = java.util.Map&lt;K, V&gt;<br/>
     * typeNameMappings = { &quot;K&quot; =&gt; &quot;java.lang.String&quot;, &quot;V&quot; =&gt; &quot;int[]&quot; }
     * </code><br/>
     * <br/>
     * Then the constructed object will represent the <code>java.util.Map&lt;java.lang.String, int[]&gt;</code> type.
     *
     * @param type             type which will be described by this object (must not be <code>null</code>)
     * @param typeNameMappings map which will be used to map type names (must not be <code>null</code>)
     * @throws NullPointerException if any of provided parameters is <code>null</code>
     */
    public TypeInformation(Type type, Map<String, String> typeNameMappings) {
        String typeString = type.getTypeName();
        String typeToHandle = extractTypeFromMap(
                Objects.requireNonNull(typeString, TYPE_NOT_NULL).trim(),
                Objects.requireNonNull(typeNameMappings, TYPE_PARAMETER_MAPPINGS_NOT_NULL)
        );
        String[] split = OPENING_DIAMOND_SPLIT_PATTERN.split(typeToHandle, 2);

        this.arrayDimension = findArrayDimension(typeToHandle);
        this.typeName = this.arrayDimension > 0
                ? ARRAY_REMOVAL_PATTERN.matcher(split[0]).replaceAll("")
                : split[0];
        this.typeParameters = split.length == 1
                ? new TypeInformation[0]
                : Arrays.stream(splitTypeParameters(
                CLOSING_DIAMOND_REMOVAL_PATTERN.matcher(split[1]).replaceAll("")))
                .map(t -> new TypeInformation(typeFromString(t), typeNameMappings))
                .toArray(TypeInformation[]::new);
    }

    /**
     * Constructs an object which will contain information about provided <code>type</code>, including its type
     * parameters.
     *
     * @param type type which will be described by this object (must not be <code>null</code>)
     * @throws NullPointerException if provided parameter is <code>null</code>
     */
    public TypeInformation(Type type) {
        this(type, Collections.emptyMap());
    }

    /**
     * Constructs an object which will contain provided type information. Type information includes type name, type
     * parameters and array dimension (if type is not an array then its dimension should be 0). Array dimension can
     * never be negative.<br/>
     * Type name may not contain whitespaces or any of the following characters: <code>&lt;&gt;[]</code>
     *
     * @param typeName       name of the type which will be described by this object (must not be <code>null</code>)
     * @param typeParameters type parameters of type described by this object (must not be <code>null</code>)
     * @param arrayDimension dimension of the array described by this object (must be non negative value)
     * @throws NullPointerException     if any of provided parameters is <code>null</code>
     * @throws IllegalArgumentException if provided <code>arrayDimension</code> is negative or if <code>typeName</code>
     *                                  string contains whitespaces or any of the following characters: <code>&lt;&gt;[]
     *                                  </code>
     */
    public TypeInformation(String typeName, TypeInformation[] typeParameters, int arrayDimension) {
        this.typeName = requireRegexConformity(
                Objects.requireNonNull(typeName, TYPE_NOT_NULL),
                TYPE_NAME_VALIDATOR, TYPE_REGEX_MUST_BE_VALID
        );
        this.typeParameters = Objects.requireNonNull(typeParameters, TYPE_PARAMETERS_NOT_NULL).clone();
        this.arrayDimension = requireNonNegative(arrayDimension, ARRAY_DIMENSION_NON_NEGATIVE);
    }

    //
    // HELPER METHODS
    //

    private static int requireNonNegative(int value, String message) {
        return requireCondition(value, v -> v >= 0, message);
    }

    private static String requireRegexConformity(String value, Predicate<String> checker, String message) {
        return requireCondition(value, checker, message);
    }

    private static <T> T requireCondition(T value, Predicate<T> condition, String message) {
        if (!condition.test(value)) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private static Type typeFromString(String typeName) {
        return new Type() { // NOSONAR this cannot be a lambda expression because java.lang.reflect.Type is not a
            // functional interface according to the Java Language Specification

            @Override
            public String getTypeName() {
                return typeName;
            }
        };
    }

    private static String extractTypeFromMap(String key, Map<String, String> typeNameMappings) {
        return typeNameMappings.getOrDefault(key, key);
    }

    private static int findArrayDimension(String typeName) {
        char[] chars = typeName.toCharArray();
        int index = chars.length - 1;
        int dimension = 0;
        boolean endsWithCorrectChar = true;

        while (endsWithCorrectChar && index >= 0) {
            if (chars[index] == ']') {
                index -= 2;
                dimension++;
            } else {
                endsWithCorrectChar = false;
            }
        }

        return dimension;
    }

    private static String[] splitTypeParameters(String typeParameters) {
        List<Integer> splitPoints = new ArrayList<>();

        splitPoints.add(-1);

        int currentDepth = 0;
        char[] chars = typeParameters.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (currentDepth == 0 && chars[i] == ',') {
                splitPoints.add(i);
            } else if (chars[i] == '<') {
                currentDepth++;
            } else if (chars[i] == '>') {
                currentDepth--;
            }
        }

        splitPoints.add(typeParameters.length());

        String[] splitTypeParameters = new String[splitPoints.size() - 1];

        for (int i = 0; i < splitTypeParameters.length; i++) {
            splitTypeParameters[i] = typeParameters.substring(splitPoints.get(i) + 1, splitPoints.get(i + 1));
        }

        return splitTypeParameters;
    }

    //
    // INSTANCE METHODS
    //

    /**
     * Fetches the name of the type described by this object, without any type parameters. For example, if this object
     * describes <code>java.util.List&lt;java.lang.String&gt;</code> then <code>&quot;java.util.List&quot;</code> string
     * will be returned.
     *
     * @return name of the type described by this object, without type parameters
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * Fetches the information about type parameters of the type described by this object. If type described by this
     * object had no type parameters, empty array will be returned. Order of type parameters is retained from the type
     * signature. For example, type parameter information array for
     * <code>java.util.Map&lt;java.lang.String, java.lang.Integer&gt;</code> will return
     * <code>[TypeInformation(java.lang.String), TypeInformation(java.lang.Integer)]</code>.
     *
     * @return an array which contains information about type parameters of the type described by this object
     */
    public TypeInformation[] getTypeParameters() {
        return this.typeParameters.clone();
    }

    /**
     * Checks if type described by this object is an array.
     *
     * @return <code>true</code> if this object describes an array, <code>false</code> otherwise
     */
    public boolean isArray() {
        return this.getArrayDimension() > 0;
    }

    /**
     * Fetches the array dimension of the type described by this object. If type described by this object is not an
     * array, return value will be <code>0</code>. Dimension of the array will never be negative.
     *
     * @return array dimension of the type described by this object
     */
    public int getArrayDimension() {
        return this.arrayDimension;
    }

    /**
     * Returns a string representation of this object. String returned by this object will be identical to string
     * returned by {@link Type#getTypeName()} for type which this object represents. If type names were mapped when
     * constructing this object (see {@link TypeInformation#TypeInformation(Type, Map)}), then mapped names will be used
     * in the string. If this object was constructed by providing custom type information (see
     * {@link TypeInformation#TypeInformation(String, TypeInformation[], int)}), then that custom type information will
     * be used in the string.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return this.typeName + this.stringifyTypeParameters() + this.stringifyArrayBrackets();
    }

    //
    // PRIVATE METHODS
    //
    private String stringifyTypeParameters() {
        if (this.typeParameters.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder("<");

        for (TypeInformation typeParameter : this.typeParameters) {
            builder.append(typeParameter)
                    .append(", ");
        }

        int length = builder.length();
        return builder.delete(length - 2, length)
                .append('>')
                .toString();
    }

    private String stringifyArrayBrackets() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < this.arrayDimension; i++) {
            builder.append("[]");
        }

        return builder.toString();
    }
}
