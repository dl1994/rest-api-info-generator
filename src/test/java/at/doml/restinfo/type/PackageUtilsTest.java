package at.doml.restinfo.type;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public final class PackageUtilsTest extends AbstractTypeVisitorMethodCallOrderTest {

    //
    // TESTS
    //
    @Test
    public void conditionalVisitShouldCallCorrectBeforeAndAfterMethods() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                TypeVisitor::visitAfterAllComplexFields
        );

        this.initializeOrderObject(callOrderInfo);

        PackageUtils.conditionalVisit(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                ignored -> false,
                ignored -> {},
                TypeVisitor::visitAfterAllComplexFields
        );

        this.assertMethodCallOrder(callOrderInfo);
    }

    @Test
    public void conditionalVisitShouldCallOnConditionMethodOnTrueCondition() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                TypeVisitor::visitBeforeArrayElementType,
                TypeVisitor::visitAfterAllComplexFields
        );

        this.initializeOrderObject(callOrderInfo);

        PackageUtils.conditionalVisit(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                ignored -> true,
                TypeVisitor::visitBeforeArrayElementType,
                TypeVisitor::visitAfterAllComplexFields
        );

        this.assertMethodCallOrder(callOrderInfo);
    }

    @Test
    public void conditionalVisitShouldCallMethodToCheckCondition() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields
        );

        this.initializeOrderObject(callOrderInfo);

        PackageUtils.conditionalVisit(
                this.mockVisitor,
                ignored -> {},
                visitor -> {
                    visitor.visitBeforeAllComplexFields();
                    return false;
                },
                ignored -> {},
                ignored -> {}
        );

        this.assertMethodCallOrder(callOrderInfo);
    }

    @Test
    public void conditionalVisitOnConditionMethodShouldNotBeCalledIfConditionIsFalse() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                TypeVisitor::visitBeforeArrayElementType,
                TypeVisitor::visitAfterAllComplexFields
        );

        this.initializeOrderObject(callOrderInfo);

        PackageUtils.conditionalVisit(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                visitor -> {
                    visitor.visitBeforeArrayElementType();
                    return false;
                },
                ignored -> fail("this was not supposed to be called"),
                TypeVisitor::visitAfterAllComplexFields
        );

        this.assertMethodCallOrder(callOrderInfo);
    }

    @Test
    public void conditionalVisitWithTypeShouldCallVisitMethodOnGivenType() {
        CallOrderInfo typeVisitorCallOrder1 = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitBeforeAllComplexFields,
                TypeVisitor::visitBeforeArrayElementType
        );
        VisitableType mockType = mock(VisitableType.class);
        CallOrderInfo typeCallOrder = this.defineRequiredCallOrderWithValue(
                mockType,
                this.mockVisitor,
                VisitableType::accept
        );
        CallOrderInfo typeVisitorCallOrder2 = this.defineRequiredCallOrder(
                this.mockVisitor,
                TypeVisitor::visitAfterAllComplexFields
        );

        this.initializeOrderObject(typeVisitorCallOrder1, typeCallOrder, typeVisitorCallOrder2);

        PackageUtils.conditionalVisitForType(
                this.mockVisitor,
                mockType,
                TypeVisitor::visitBeforeAllComplexFields,
                visitor -> {
                    visitor.visitBeforeArrayElementType();
                    return true;
                },
                TypeVisitor::visitAfterAllComplexFields
        );

        this.assertMethodCallOrder(typeVisitorCallOrder1, typeCallOrder, typeVisitorCallOrder2);
    }
}
