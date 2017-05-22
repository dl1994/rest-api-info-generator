package at.doml.restinfo.type;

import at.doml.restinfo.TypeInformation;
import org.junit.Test;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import static org.mockito.Mockito.when;

public final class TypeVisitorTest extends AbstractTypeVisitorMethodCallOrderTest {

    private VisitableType type;

    //
    // TESTS
    //
    @Test
    public void arrayTypeShouldCallCorrectVisitMethods() {
        this.type = new ArrayType(SimpleType.INT);
        this.testIfArrayOrCollectionTypeHaveCorrectCallOrder(
                TypeVisitor::visitBeforeArrayElementType,
                TypeVisitor::visitAfterArrayElementType,
                TypeVisitor::shouldVisitArrayElementType
        );
    }

    @Test
    public void collectionTypeShouldCallCorrectVisitMethods() {
        this.type = new CollectionType(SimpleType.INT);
        this.testIfArrayOrCollectionTypeHaveCorrectCallOrder(
                TypeVisitor::visitBeforeCollectionElementType,
                TypeVisitor::visitAfterCollectionElementType,
                TypeVisitor::shouldVisitCollectionElementType
        );
    }

    @Test
    public void complexTypeShouldCallCorrectVisitMethods() {
        this.type = new ComplexType();

        String fieldName = "number";
        ((ComplexType) this.type).addField(fieldName, SimpleType.INT);
        CallOrderInfo callOrderInfo1 = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields
        );
        CallOrderInfo callOrderInfo2 = this.defineRequiredCallOrderWithValue(
                this.mockVisitor,
                fieldName,
                TypeVisitor::visitBeforeComplexField,
                TypeVisitor::visitAfterComplexField
        );
        CallOrderInfo callOrderInfo3 = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitAfterAllComplexFields
        );
        CallOrderInfo conditionalInfo1 = this.defineConditionalCallOrder(
                TypeVisitor::shouldVisitComplexFields
        );
        CallOrderInfo conditionalInfo2 = this.defineConditionalCallOrder(
                fieldName,
                TypeVisitor::shouldVisitComplexFieldType
        );

        when(this.mockVisitor.shouldVisitComplexFields()).thenReturn(true);
        this.initializeOrderObject(callOrderInfo1, callOrderInfo2, callOrderInfo3);
        this.initializeConditionalOrderObject(conditionalInfo1, conditionalInfo2);
        this.type.accept(this.mockVisitor);
        this.assertMethodCallOrder(callOrderInfo1, callOrderInfo2, callOrderInfo3);
        this.assertConditionalCallOrder(conditionalInfo1, conditionalInfo2);
    }

    @Test
    public void mapTypeShouldCallCorrectVisitMethods() {
        this.type = new MapType(SimpleType.INT, SimpleType.STRING);

        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeMapKeyType,
                TypeVisitor::visitAfterMapKeyType,
                TypeVisitor::visitBeforeMapValueType,
                TypeVisitor::visitAfterMapValueType
        );
        CallOrderInfo conditionalInfo = this.defineConditionalCallOrder(
                TypeVisitor::shouldVisitMapKeyType,
                TypeVisitor::shouldVisitMapValueType
        );

        this.initializeOrderObject(callOrderInfo);
        this.initializeConditionalOrderObject(conditionalInfo);
        this.type.accept(this.mockVisitor);
        this.assertMethodCallOrder(callOrderInfo);
        this.assertConditionalCallOrder(conditionalInfo);
    }

    @Test
    public void customTypeShouldCallCorrectVisitMethod() {
        TypeInformation testTypeInformation = new TypeInformation("int", new TypeInformation[0], 0);

        this.type = new CustomType(testTypeInformation);
        this.callVisitMethodAndAssertThatCorrectMethodWasCalled(TypeVisitor::visitCustom, testTypeInformation);
    }

    @Test
    public void unknownTypeShouldCallCorrectVisitMethod() {
        TypeInformation testTypeInformation = new TypeInformation("unknown", new TypeInformation[0], 0);

        this.type = new UnknownType(testTypeInformation);
        this.callVisitMethodAndAssertThatCorrectMethodWasCalled(TypeVisitor::visitUnknown, testTypeInformation);
    }

    @Test
    public void enumTypeShouldCallCorrectVisitMethod() {
        this.type = new EnumType(TestEnum.values());
        this.callVisitMethodAndAssertThatCorrectMethodWasCalled(TypeVisitor::visitEnum, TestEnum.values());
    }

    @Test
    public void simpleTypeShouldCallCorrectVisitMethod() {
        this.type = SimpleType.INT;
        this.callVisitMethodAndAssertThatCorrectMethodWasCalled(TypeVisitor::visitSimple, SimpleType.INT);
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
    private void callVisitMethodAndAssertThatCorrectMethodWasCalled(Consumer<TypeVisitor> expectedMethod) {
        this.verifyCallOrderForSingleMethod(
                this.defineRequiredCallOrder(this.mockVisitor, expectedMethod)
        );
    }

    private <U> void callVisitMethodAndAssertThatCorrectMethodWasCalled(BiConsumer<TypeVisitor, U> expectedMethod,
                                                                        U argument) {
        this.verifyCallOrderForSingleMethod(
                this.defineRequiredCallOrder(
                        this.mockVisitor,
                        visitor -> expectedMethod.accept(visitor, argument)
                )
        );
    }

    private void verifyCallOrderForSingleMethod(CallOrderInfo callOrderInfo) {
        this.initializeOrderObject(callOrderInfo);
        this.type.accept(this.mockVisitor);
        this.assertMethodCallOrder(callOrderInfo);
    }

    private void testIfArrayOrCollectionTypeHaveCorrectCallOrder(Consumer<TypeVisitor> firstCall,
                                                                 Consumer<TypeVisitor> secondCall,
                                                                 Predicate<TypeVisitor> conditional) {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockVisitor,
                firstCall,
                secondCall
        );
        CallOrderInfo conditionalInfo = this.defineConditionalCallOrder(
                conditional
        );

        this.initializeOrderObject(callOrderInfo);
        this.initializeConditionalOrderObject(conditionalInfo);
        this.type.accept(this.mockVisitor);
        this.assertMethodCallOrder(callOrderInfo);
        this.assertConditionalCallOrder(conditionalInfo);
    }
}
