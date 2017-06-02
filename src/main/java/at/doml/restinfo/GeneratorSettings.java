package at.doml.restinfo;

import at.doml.restinfo.type.TypeTreeGenerator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class GeneratorSettings {

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    final Set<Class<?>> excludedControllers;
    final TypeTreeGenerator typeTreeGenerator;
    final Function<String, String> apiSectionNamingStrategy;

    private GeneratorSettings(Set<Class<?>> excludedControllers, TypeTreeGenerator typeTreeGenerator,
                              Function<String, String> apiSectionNamingStrategy) {
        this.excludedControllers = excludedControllers;
        this.typeTreeGenerator = typeTreeGenerator;
        this.apiSectionNamingStrategy = apiSectionNamingStrategy;
    }

    //
    // BUILDER
    //
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        //
        // CONSTANTS
        //
        private static final String NOT_NULL = " must not be null";
        private static final String CONTROLLER = "controller";
        private static final String CONTROLLERS = CONTROLLER + 's';
        private static final String CONTROLLER_NOT_NULL = CONTROLLER + NOT_NULL;
        private static final String CONTROLLERS_NOT_NULL = CONTROLLERS + NOT_NULL;
        private static final String CONTROLLERS_ELEMENT_NOT_NULL = CONTROLLERS + " elements" + NOT_NULL;
        private static final String TYPE_TREE_GENERATOR_NOT_NULL = "typeTreeGenerator" + NOT_NULL;
        private static final String API_SECTION_NAMING_STRTEGY_NOT_NULL = "apiSectionNamingStrategy" + NOT_NULL;
        private static final String EMPTY = "";
        private static final String REPLACEMENT_STRING = "$1 $2";
        private static final Pattern SPLIT_PATTERN = Pattern.compile("(.)(\\p{javaUpperCase})");
        private static final Pattern CONTROLLER_REMOVAL_PATTERN = Pattern.compile("Controller$");
        private static final Function<String, String> DEFAULT_API_SECTION_NAMING_STRATEGY = controllerFullName ->
                SPLIT_PATTERN.matcher(
                        CONTROLLER_REMOVAL_PATTERN.matcher(controllerFullName).replaceFirst(EMPTY)
                ).replaceAll(REPLACEMENT_STRING);

        //
        // CONSTRUCTORS AND MEMBER VARIABLES
        //
        private TypeTreeGenerator typeTreeGenerator;
        private Function<String, String> apiSectionNamingStrategy;
        private final Set<Class<?>> excludedControllers;

        private Builder() {
            this.excludedControllers = new HashSet<>();
            this.typeTreeGenerator = new TypeTreeGenerator();
            this.apiSectionNamingStrategy = DEFAULT_API_SECTION_NAMING_STRATEGY;
        }

        //
        // INSTANCE METHODS
        //
        public Builder exclude(Class<?> controller, Class<?>... controllers) {
            this.excludedControllers.add(Objects.requireNonNull(controller, CONTROLLER_NOT_NULL));

            for (Class<?> clazz : Objects.requireNonNull(controllers, CONTROLLERS_NOT_NULL)) {
                this.excludedControllers.add(Objects.requireNonNull(clazz, CONTROLLERS_ELEMENT_NOT_NULL));
            }

            return this;
        }

        public Builder typeTreeGenerator(TypeTreeGenerator typeTreeGenerator) {
            this.typeTreeGenerator = Objects.requireNonNull(typeTreeGenerator, TYPE_TREE_GENERATOR_NOT_NULL);
            return this;
        }

        public Builder apiSectionNamingStrategy(Function<String, String> apiSectionNamingStrategy) {
            this.apiSectionNamingStrategy = Objects.requireNonNull(apiSectionNamingStrategy,
                    API_SECTION_NAMING_STRTEGY_NOT_NULL);
            return this;
        }

        public GeneratorSettings build() {
            return new GeneratorSettings(this.excludedControllers, this.typeTreeGenerator,
                    this.apiSectionNamingStrategy);
        }
    }
}
