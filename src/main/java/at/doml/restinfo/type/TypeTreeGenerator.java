package at.doml.restinfo.type;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class TypeTreeGenerator {
    
    private final Mode mode;
    private final Set<Class<?>> customClasses = new HashSet<>();
    private static final Mode DEFAULT_MODE = Mode.EXTRACT_BOTH;
    
    public enum Mode {
        NONE, EXTRACT_GETTERS, EXTRACT_SETTERS, EXTRACT_BOTH
    }
    
    public TypeTreeGenerator() {
        this(DEFAULT_MODE);
    }
    
    public TypeTreeGenerator(Mode mode) {
        this.mode = mode;
    }
    
    public void registerCustomClass(Class<?> clazz) {
        this.customClasses.add(clazz);
    }
    
    public void removeCustomClass(Class<?> clazz) {
        this.customClasses.remove(clazz);
    }
    
    public VisitableType generateTree(Type type) {
        return null; // TODO
    }
}
