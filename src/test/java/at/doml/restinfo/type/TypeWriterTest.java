package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import at.doml.restinfo.TypeWriter;
import org.junit.Test;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import static org.mockito.Mockito.when;

public final class TypeWriterTest extends AbstractTypeWriterMethodCallOrderTest {
    
    private WritableType type;
    
    //
    // TESTS
    //
    @Test
    public void arrayTypeShouldCallCorrectWriteMethods() {
        this.type = new ArrayType(SimpleType.INT);
        this.testIfArrayOrCollectionTypeHaveCorrectCallOrder(
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::writeAfterArrayElementType,
                TypeWriter::shouldWriteArrayElementType
        );
    }
    
    @Test
    public void collectionTypeShouldCallCorrectWriteMethods() {
        this.type = new CollectionType(SimpleType.INT);
        this.testIfArrayOrCollectionTypeHaveCorrectCallOrder(
                TypeWriter::writeBeforeCollectionElementType,
                TypeWriter::writeAfterCollectionElementType,
                TypeWriter::shouldWriteCollectionElementType
        );
    }
    
    @Test
    public void complexTypeShouldCallCorrectWriteMethods() {
        this.type = new ComplexType();
        
        String fieldName = "number";
        ((ComplexType) this.type).addField(fieldName, SimpleType.INT);
        CallOrderInfo callOrderInfo1 = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeAllComplexFields
        );
        CallOrderInfo callOrderInfo2 = this.defineRequiredCallOrderWithValue(
                this.mockWriter,
                fieldName,
                TypeWriter::writeBeforeComplexField,
                TypeWriter::writeAfterComplexField
        );
        CallOrderInfo callOrderInfo3 = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeAfterAllComplexFields
        );
        CallOrderInfo conditionalInfo = this.defineConditionalCallOrder(
                TypeWriter::shouldWriteComplexFields,
                TypeWriter::shouldWriteComplexFieldType
        );
        
        when(this.mockWriter.shouldWriteComplexFields()).thenReturn(true);
        this.initializeOrderObject(callOrderInfo1, callOrderInfo2, callOrderInfo3);
        this.initializeConditionalOrderObject(conditionalInfo);
        this.type.write(this.mockWriter);
        this.assertMethodCallOrder(callOrderInfo1, callOrderInfo2, callOrderInfo3);
        this.assertConditionalCallOrder(conditionalInfo);
    }
    
    @Test
    public void mapTypeShouldCallCorrectWriteMethods() {
        this.type = new MapType(SimpleType.INT, SimpleType.STRING);
        
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::writeBeforeMapKeyType,
                TypeWriter::writeAfterMapKeyType,
                TypeWriter::writeBeforeMapValueType,
                TypeWriter::writeAfterMapValueType
        );
        CallOrderInfo conditionalInfo = this.defineConditionalCallOrder(
                TypeWriter::shouldWriteMapKeyType,
                TypeWriter::shouldWriteMapValueType
        );
        
        this.initializeOrderObject(callOrderInfo);
        this.initializeConditionalOrderObject(conditionalInfo);
        this.type.write(this.mockWriter);
        this.assertMethodCallOrder(callOrderInfo);
        this.assertConditionalCallOrder(conditionalInfo);
    }
    
    @Test
    public void customTypeShouldCallCorrectWriteMethod() {
        TypeInformation testTypeInformation = new TypeInformation("int");
        
        this.type = new CustomType(testTypeInformation);
        this.callWriteMethodAndAssertThatCorrectMethodWasCalled(TypeWriter::writeCustom, testTypeInformation);
    }
    
    @Test
    public void enumTypeShouldCallCorrectWriteMethod() {
        this.type = new EnumType(TestEnum.values());
        this.callWriteMethodAndAssertThatCorrectMethodWasCalled(TypeWriter::writeEnum, TestEnum.values());
    }
    
    @Test
    public void simpleTypeShouldCallCorrectWriteMethod() {
        this.type = SimpleType.INT;
        this.callWriteMethodAndAssertThatCorrectMethodWasCalled(TypeWriter::writeSimple, SimpleType.INT);
    }
    
    //
    // PRIVATE CLASSES
    //
    private enum TestEnum {
        TEST_VALUE
    }
    
    //
    // HELPER METHODS
    //
    private void callWriteMethodAndAssertThatCorrectMethodWasCalled(Consumer<TypeWriter> expectedMethod) {
        this.verifyCallOrderForSingleMethod(
                this.defineRequiredCallOrder(this.mockWriter, expectedMethod)
        );
    }
    
    private <U> void callWriteMethodAndAssertThatCorrectMethodWasCalled(BiConsumer<TypeWriter, U> expectedMethod,
                                                                        U argument) {
        this.verifyCallOrderForSingleMethod(
                this.defineRequiredCallOrder(
                        this.mockWriter,
                        writer -> expectedMethod.accept(writer, argument)
                )
        );
    }
    
    private void verifyCallOrderForSingleMethod(CallOrderInfo callOrderInfo) {
        this.initializeOrderObject(callOrderInfo);
        this.type.write(this.mockWriter);
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    private void testIfArrayOrCollectionTypeHaveCorrectCallOrder(Consumer<TypeWriter> firstCall,
                                                                 Consumer<TypeWriter> secondCall,
                                                                 Function<TypeWriter, Boolean> conditional) {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                firstCall,
                secondCall
        );
        CallOrderInfo conditionalInfo = this.defineConditionalCallOrder(
                conditional
        );
        
        this.initializeOrderObject(callOrderInfo);
        this.initializeConditionalOrderObject(conditionalInfo);
        this.type.write(this.mockWriter);
        this.assertMethodCallOrder(callOrderInfo);
        this.assertConditionalCallOrder(conditionalInfo);
    }
}
