package at.doml.restinfo;

import at.doml.restinfo.type.SimpleType;
import at.doml.restinfo.type.TypeVisitor;
import java.io.IOException;
import java.util.Objects;

public abstract class AbstractTypeTreeWriter implements TypeVisitor {

    //
    // CONSTANTS
    //
    private static final String STRING_APPENDER_NOT_NULL = "stringAppender must not be null";

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    protected final Appendable stringAppender;

    protected AbstractTypeTreeWriter(Appendable stringAppender) {
        this.stringAppender = Objects.requireNonNull(stringAppender, STRING_APPENDER_NOT_NULL);
    }

    //
    // HELPER METHODS
    //
    private static void tryCall(IOCallable callable) {
        try {
            callable.call();
        } catch (IOException exception) {
            throw new TypeWriterException(exception);
        }
    }

    //
    // UTIL CLASSES AND INTERFACES
    //
    private interface IOCallable {
        void call() throws IOException;
    }

    //
    // INSTANCE METHODS
    //
    @Override
    public final void visitSimple(SimpleType type) {
        tryCall(() -> this.writeSimple(type));
    }

    @Override
    public final void visitEnum(Enum<?>[] enumConstants) {
        tryCall(() -> this.writeEnum(enumConstants));
    }

    @Override
    public final void visitBeforeArrayElementType() {
        tryCall(this::writeBeforeArrayElementType);
    }

    @Override
    public final void visitAfterArrayElementType() {
        tryCall(this::writeAfterArrayElementType);
    }

    @Override
    public final void visitBeforeCollectionElementType() {
        tryCall(this::writeBeforeCollectionElementType);
    }

    @Override
    public final void visitAfterCollectionElementType() {
        tryCall(this::writeAfterCollectionElementType);
    }

    @Override
    public final void visitBeforeMapKeyType() {
        tryCall(this::writeBeforeMapKeyType);
    }

    @Override
    public final void visitAfterMapKeyType() {
        tryCall(this::writeAfterMapKeyType);
    }

    @Override
    public final void visitBeforeMapValueType() {
        tryCall(this::writeBeforeMapValueType);
    }

    @Override
    public final void visitAfterMapValueType() {
        tryCall(this::writeAfterMapValueType);
    }

    @Override
    public final void visitBeforeAllComplexFields() {
        tryCall(this::writeBeforeAllComplexFields);
    }

    @Override
    public final void visitBeforeComplexField(String fieldName) {
        tryCall(() -> this.writeBeforeComplexField(fieldName));
    }

    @Override
    public final void visitAfterComplexField(String fieldName) {
        tryCall(() -> this.writeAfterComplexField(fieldName));
    }

    @Override
    public final void visitAfterAllComplexFields() {
        tryCall(this::writeAfterAllComplexFields);
    }

    @Override
    public final void visitCustom(TypeInformation customTypeInformation) {
        tryCall(() -> this.writeCustom(customTypeInformation));
    }

    @Override
    public final void visitUnknown(TypeInformation unknownTypeInformation) {
        tryCall(() -> this.writeUnknown(unknownTypeInformation));
    }

    //
    // ABSTRACT METHODS
    //
    protected abstract void writeSimple(SimpleType type) throws IOException;

    protected abstract void writeEnum(Enum<?>[] enumConstants) throws IOException;

    protected abstract void writeBeforeArrayElementType() throws IOException;

    protected abstract void writeAfterArrayElementType() throws IOException;

    protected abstract void writeBeforeCollectionElementType() throws IOException;

    protected abstract void writeAfterCollectionElementType() throws IOException;

    protected abstract void writeBeforeMapKeyType() throws IOException;

    protected abstract void writeAfterMapKeyType() throws IOException;

    protected abstract void writeBeforeMapValueType() throws IOException;

    protected abstract void writeAfterMapValueType() throws IOException;

    protected abstract void writeBeforeAllComplexFields() throws IOException;

    protected abstract void writeBeforeComplexField(String fieldName) throws IOException;

    protected abstract void writeAfterComplexField(String fieldName) throws IOException;

    protected abstract void writeAfterAllComplexFields() throws IOException;

    protected abstract void writeCustom(TypeInformation customTypeInformation) throws IOException;

    protected abstract void writeUnknown(TypeInformation unknownTypeInformation) throws IOException;
}
