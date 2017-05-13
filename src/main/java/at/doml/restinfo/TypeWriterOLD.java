package at.doml.restinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class TypeWriterOLD {
    
    //
    // Constants
    //
    private static final String OBJECT_STRING = "{}";
    private static final String NUMBER_STRING = "<b class=\"number\">0</b>";
    private static final String DECIMAL_STRING = "<b class=\"number\">0.0</b>";
    private static final String BOOLEAN_STRING = "<b class=\"keyword\">true</b> | <b class=\"keyword\">false</b>";
    private static final String STRING_STRING = "<b class=\"string\">\"string\"</b>";
    private static final String CHARACTER_STRING = "<b class=\"string\">'c'</b>";
    private static final Map<String, String> BASIC_WRITERS = new HashMap<>();
    private static final CustomClassFieldFetcher GETTER_FETCHER = new GetterFieldFetcher();
    private static final CustomClassFieldFetcher SETTER_FETCHER = new SetterFieldFetcher();
    
    //
    // Static initializers
    //
    static {
        addBasicWriterFor(OBJECT_STRING, Object.class);
        addBasicWriterFor(NUMBER_STRING,
                Byte.class, Short.class, Integer.class, Long.class, BigInteger.class,
                byte.class, short.class, int.class, long.class
        );
        addBasicWriterFor(DECIMAL_STRING,
                Float.class, Double.class, BigDecimal.class,
                float.class, double.class
        );
        addBasicWriterFor(BOOLEAN_STRING,
                Boolean.class,
                boolean.class
        );
        addBasicWriterFor(STRING_STRING, String.class);
        addBasicWriterFor(CHARACTER_STRING,
                Character.class,
                char.class
        );
    }
    
    //
    // Private classes and interfaces
    //
    private interface CustomClassFieldFetcher {
        boolean canFetchFrom(Method method);
        
        TypeInformation fetchField(Method method, Map<String, String> typeParameterMappings);
    }
    
    private static class GetterFieldFetcher implements CustomClassFieldFetcher {
        
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
    
    private static class SetterFieldFetcher implements CustomClassFieldFetcher {
        
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
    
    //
    // Static utility methods
    //
    private static boolean isVoid(Method method) {
        Class<?> returnType = method.getReturnType();
        return Objects.equals(returnType, void.class) || Objects.equals(returnType, Void.class);
    }
    
    private static String getFieldName(String methodName) {
        String withoutGetterSetterPrefix = methodName.replaceAll("^(get|is|set)", "");
        
        if (withoutGetterSetterPrefix.length() > 1) {
            return Character.toLowerCase(withoutGetterSetterPrefix.charAt(0)) + withoutGetterSetterPrefix.substring(1);
        }
        
        return withoutGetterSetterPrefix.toLowerCase();
    }
    
    private static void addBasicWriterFor(String value, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            BASIC_WRITERS.put(clazz.getTypeName(), value);
        }
    }
    
    private static String indent(int indentLevel) {
        StringBuilder stringBuilder = new StringBuilder();
        
        for (int i = 0; i < indentLevel; i++) {
            stringBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
        
        return stringBuilder.toString();
    }
    
    //
    // TODO: this will be instance methods
    //
    public static String stringifyGetters(Type type) {
        return stringify(type, GETTER_FETCHER);
    }
    
    public static String stringifySetters(Type type) {
        return stringify(type, SETTER_FETCHER);
    }
    
    private static String stringify(Type type, CustomClassFieldFetcher fieldFetcher) {
        return stringify(new TypeInformation(type.getTypeName()), fieldFetcher, 1);
    }
    
    private static String stringify(TypeInformation typeInformation,
                                    CustomClassFieldFetcher fieldFetcher, int indentLevel) {
        if (typeInformation.isArray()) {
            return writeArray(typeInformation, fieldFetcher, indentLevel);
        }
        
        String basic = BASIC_WRITERS.get(typeInformation.getType());
        
        if (basic != null) {
            return basic;
        }
        
        Class<?> clazz;
        
        try {
            clazz = Class.forName(typeInformation.getType());
        } catch (ClassNotFoundException ignored) {
            return "?";
        }
        
        if (clazz.isEnum()) {
            return writeEnum(clazz);
        }
        
        if (Collection.class.isAssignableFrom(clazz)) {
            return writeCollection(typeInformation, fieldFetcher, indentLevel);
        }
        
        if (Map.class.isAssignableFrom(clazz)) {
            return writeMap(typeInformation, fieldFetcher, indentLevel);
        }
        
        return writeClass(clazz, typeInformation, fieldFetcher, indentLevel);
    }
    
    private static String writeEnum(Class<?> clazz) {
        Object[] constants = clazz.getEnumConstants();
        StringBuilder stringBuilder = new StringBuilder("enum(");
        
        for (Object constant : constants) {
            stringBuilder.append("<b class=\"string\">\"")
                    .append(((Enum<?>) constant).name())
                    .append("\"</b>")
                    .append(", ");
        }
        
        if (constants.length != 0) {
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }
        
        return stringBuilder.append(')').toString();
    }
    
    private static String writeCollection(TypeInformation typeInformation,
                                          CustomClassFieldFetcher fieldFetcher, int indentLevel) {
        return '[' + stringify(typeInformation.getTypeParameters()[0], fieldFetcher, indentLevel) + ", ...]";
    }
    
    private static String writeArray(TypeInformation typeInformation,
                                     CustomClassFieldFetcher fieldFetcher, int indentLevel) {
        return '[' + stringify(new TypeInformation(
                typeInformation.getType(), typeInformation.getTypeParameters(),
                typeInformation.getArrayDimension() - 1
        ), fieldFetcher, indentLevel) + ", ...]";
    }
    
    private static String writeMap(TypeInformation typeInformation,
                                   CustomClassFieldFetcher fieldFetcher, int indentLevel) {
        TypeInformation[] typeParameters = typeInformation.getTypeParameters();
        TypeInformation keyType = typeParameters[0];
        TypeInformation valueType = typeParameters[1];
        String indent = indent(indentLevel);
        
        return "{<br/>" + indent +
                stringify(keyType, fieldFetcher, indentLevel + 1) +
                ": " +
                stringify(valueType, fieldFetcher, indentLevel + 1) +
                ',' +
                "<br/>" + indent + "...<br/>" + indent(indentLevel - 1) + '}';
    }
    
    private static String writeClass(Class<?> clazz, TypeInformation typeInformation,
                                     CustomClassFieldFetcher fieldFetcher, int indentLevel) {
        Field[] publicFields = clazz.getFields();
        Method[] publicMethods = clazz.getMethods();
        Map<String, String> fields = new HashMap<>();
        Map<String, String> typeParameterMappings = new HashMap<>();
        TypeVariable<?>[] genericTypeParameters = clazz.getTypeParameters();
        TypeInformation[] actualTypeParameters = typeInformation.getTypeParameters();
        
        int limit = Math.min(genericTypeParameters.length, actualTypeParameters.length);
        for (int i = 0; i < limit; i++) {
            typeParameterMappings.put(genericTypeParameters[i].getTypeName(), actualTypeParameters[i].getType());
        }
        
        for (Field publicField : publicFields) {
            fields.put(publicField.getName(), stringify(
                    new TypeInformation(publicField.getGenericType().getTypeName(), typeParameterMappings),
                    fieldFetcher, indentLevel + 1
            ));
        }
        
        for (Method publicMethod : publicMethods) {
            if (fieldFetcher.canFetchFrom(publicMethod)) {
                fields.put(getFieldName(publicMethod.getName()), stringify(
                        fieldFetcher.fetchField(publicMethod, typeParameterMappings), fieldFetcher, indentLevel + 1
                ));
            }
        }
        
        StringBuilder stringBuilder = new StringBuilder()
                .append('{');
        String indent = indent(indentLevel);
        
        fields.forEach((name, value) -> {
            stringBuilder.append("<br/>")
                    .append(indent)
                    .append("<b class=\"property\">\"")
                    .append(name)
                    .append("\"</b>: ")
                    .append(value)
                    .append(',');
        });
        
        if (stringBuilder.length() > 1) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.append("<br>");
        }
        
        stringBuilder.append(indent(indentLevel - 1));
        stringBuilder.append('}');
        
        return stringBuilder.toString();
    }
}
