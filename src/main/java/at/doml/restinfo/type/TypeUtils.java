package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;
import java.util.function.Consumer;
import java.util.function.Function;

final class TypeUtils {
    
    private TypeUtils() {
        // No instances of this class are possible
    }
    
    static void conditionalWrite(TypeWriter writer, Type type, Consumer<TypeWriter> writeBefore,
                                 Function<TypeWriter, Boolean> writeTypeCondition, Consumer<TypeWriter> writeAfter) {
        conditionalWrite(writer, writeBefore, writeTypeCondition, type::write, writeAfter);
    }
    
    static void conditionalWrite(TypeWriter writer, Consumer<TypeWriter> writeBefore,
                                 Function<TypeWriter, Boolean> condition, Consumer<TypeWriter> onCondition,
                                 Consumer<TypeWriter> writeAfter) {
        writeBefore.accept(writer);
        
        if (condition.apply(writer)) {
            onCondition.accept(writer);
        }
        
        writeAfter.accept(writer);
    }
}
