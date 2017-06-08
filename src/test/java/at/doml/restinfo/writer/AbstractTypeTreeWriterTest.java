package at.doml.restinfo.writer;

import at.doml.restinfo.type.SimpleType;
import at.doml.restinfo.type.TypeInformation;
import org.junit.Test;
import java.io.IOException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class AbstractTypeTreeWriterTest {

    private final AbstractTypeTreeWriter writer = mock(AbstractTypeTreeWriter.class);

    @Test
    public void visitSimpleShouldCallWriteSimple() throws IOException {
        SimpleType argument = SimpleType.BIGINT;
        this.writer.visitSimple(argument);
        verify(this.writer).writeSimple(argument);
    }

    @Test
    public void visitEnumShouldCallWriteEnum() throws IOException {
        SimpleType[] argument = SimpleType.values();
        this.writer.visitEnum(argument);
        verify(this.writer).writeEnum(argument);
    }

    @Test
    public void visitBeforeArrayElementTypeShouldCallWriteBeforeArrayElementType() throws IOException {
        this.writer.visitBeforeArrayElementType();
        verify(this.writer).writeBeforeArrayElementType();
    }

    @Test
    public void visitAfterArrayElementTypeShouldCallWriteAfterArrayElementType() throws IOException {
        this.writer.visitAfterArrayElementType();
        verify(this.writer).writeAfterArrayElementType();
    }

    @Test
    public void visitBeforeCollectionElementTypeShouldCallWriteBeforeCollectionElementType() throws IOException {
        this.writer.visitBeforeCollectionElementType();
        verify(this.writer).writeBeforeCollectionElementType();
    }

    @Test
    public void visitAfterCollectionElementTypeShouldCallWriteAfterCollectionElementType() throws IOException {
        this.writer.visitAfterCollectionElementType();
        verify(this.writer).writeAfterCollectionElementType();
    }

    @Test
    public void visitBeforeMapKeyTypeShouldCallWriteBeforeMapKeyType() throws IOException {
        this.writer.visitBeforeMapKeyType();
        verify(this.writer).writeBeforeMapKeyType();
    }

    @Test
    public void visitAfterMapKeyTypeShouldCallWriteAfterMapKeyType() throws IOException {
        this.writer.visitAfterMapKeyType();
        verify(this.writer).writeAfterMapKeyType();
    }

    @Test
    public void visitBeforeMapValueTypeShouldCallWriteBeforeMapValueType() throws IOException {
        this.writer.visitBeforeMapValueType();
        verify(this.writer).writeBeforeMapValueType();
    }

    @Test
    public void visitAfterMapValueTypeShouldCallWriteAfterMapValueType() throws IOException {
        this.writer.visitAfterMapValueType();
        verify(this.writer).writeAfterMapValueType();
    }

    @Test
    public void visitBeforeAllComplexFieldsShouldCallWriteBeforeAllComplexFields() throws IOException {
        this.writer.visitBeforeAllComplexFields();
        verify(this.writer).writeBeforeAllComplexFields();
    }

    @Test
    public void visitBeforeComplexFieldShouldCallWriteBeforeComplexField() throws IOException {
        String argument = "fieldName";
        this.writer.visitBeforeComplexField(argument);
        verify(this.writer).writeBeforeComplexField(argument);
    }

    @Test
    public void visitAfterComplexFieldShouldCallWriteAfterComplexField() throws IOException {
        String argument = "fieldName";
        this.writer.visitAfterComplexField(argument);
        verify(this.writer).writeAfterComplexField(argument);
    }

    @Test
    public void visitAfterAllComplexFieldsShouldCallWriteAfterAllComplexFields() throws IOException {
        this.writer.visitAfterAllComplexFields();
        verify(this.writer).writeAfterAllComplexFields();
    }

    @Test
    public void visitCustomShouldCallWriteCustom() throws IOException {
        TypeInformation argument = new TypeInformation("int", new TypeInformation[0], 0);
        this.writer.visitCustom(argument);
        verify(this.writer).writeCustom(argument);
    }

    @Test
    public void visitUnknownShouldCallWriteUnknown() throws IOException {
        TypeInformation argument = new TypeInformation("int", new TypeInformation[0], 0);
        this.writer.visitUnknown(argument);
        verify(this.writer).writeUnknown(argument);
    }
}
