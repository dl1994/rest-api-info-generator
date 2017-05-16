package at.doml.restinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class TypeInformation {
    
    private final int arrayDimension;
    private final String type;
    private final TypeInformation[] typeParameters;
    
    private static final Pattern ARRAY_REMOVAL_PATTERN = Pattern.compile("(\\[])*$");
    private static final Pattern CLOSING_DIAMOND_REMOVAL_PATTERN = Pattern.compile(">(\\[])*$");
    
    public TypeInformation(String type, Map<String, String> typeParameterMappings) {
        String typeToHandle = extractTypeFromMap(type.trim(), typeParameterMappings);
        String[] split = typeToHandle.split("<", 2);
        
        this.arrayDimension = findArrayDimension(typeToHandle);
        this.type = this.arrayDimension > 0
                ? ARRAY_REMOVAL_PATTERN.matcher(split[0]).replaceAll("")
                : split[0];
        this.typeParameters = split.length == 1
                ? new TypeInformation[0]
                : Arrays.stream(splitTypeParameters(
                CLOSING_DIAMOND_REMOVAL_PATTERN.matcher(split[1]).replaceAll("")))
                .map(t -> new TypeInformation(t, typeParameterMappings))
                .toArray(TypeInformation[]::new);
    }
    
    public TypeInformation(String type) {
        this(type, Collections.emptyMap());
    }
    
    public TypeInformation(String type, TypeInformation[] typeParameters, int arrayDimension) {
        this.arrayDimension = arrayDimension;
        this.type = type;
        this.typeParameters = typeParameters.clone();
    }
    
    private static String extractTypeFromMap(String key, Map<String, String> typeParameterMappings) {
        return typeParameterMappings.getOrDefault(key, key);
    }
    
    private static int findArrayDimension(String type) {
        char[] chars = type.toCharArray();
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
    
    public String getType() {
        return this.type;
    }
    
    public TypeInformation[] getTypeParameters() {
        return this.typeParameters.clone();
    }
    
    public boolean isArray() {
        return this.getArrayDimension() > 0;
    }
    
    public int getArrayDimension() {
        return this.arrayDimension;
    }
    
    @Override
    public String toString() {
        return this.type + this.stringifyTypeParameters() + this.stringifyArrayBrackets();
    }
    
    private String stringifyTypeParameters() {
        if (this.typeParameters.length == 0) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder("<");
        
        for (TypeInformation typeParameter : this.typeParameters) {
            builder.append(typeParameter.toString())
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
