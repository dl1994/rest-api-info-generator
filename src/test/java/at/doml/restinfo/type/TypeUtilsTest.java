package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public final class TypeUtilsTest extends AbstractTypeWriterMethodCallOrderTest {
    
    //
    // TESTS
    //
    @Test
    public void conditionalWriteShouldCallCorrectBeforeAndAfterMethods() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                ignored -> false,
                ignored -> {},
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteShouldCallOnConditionMethodOnTrueCondition() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                ignored -> true,
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteShouldCallMethodToCheckCondition() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields
        );
        
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                ignored -> {},
                writer -> {
                    writer.writeBeforeAllComplexFields();
                    return false;
                },
                ignored -> {},
                ignored -> {}
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteOnConditionMethodShouldNotBeCalledIfConditionIsFalse() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                writer -> {
                    writer.writeBeforeArrayElementType();
                    return false;
                },
                ignored -> fail("this was not supposed to be called"), // should not be called
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteWithTypeShouldCallWriteMethodOnGivenType() {
        CallOrderInfo typeWriterCallOrder1 = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields,
                TypeWriter::writeBeforeArrayElementType
        );
        Type mockType = mock(Type.class);
        CallOrderInfo typeCallOrder = this.defineRequiredCallOrderWithValue(
                mockType,
                this.mockWriter,
                Type::write
        );
        CallOrderInfo typeWriterCallOrder2 = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.initializeOrderObject(typeWriterCallOrder1, typeCallOrder, typeWriterCallOrder2);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                mockType,
                TypeWriter::writeBeforeAllComplexFields,
                writer -> {
                    writer.writeBeforeArrayElementType();
                    return true;
                },
                TypeWriter::writeAfterAllComplexFields
        );
        
        this.assertMethodCallOrder(typeWriterCallOrder1, typeCallOrder, typeWriterCallOrder2);
    }
}
