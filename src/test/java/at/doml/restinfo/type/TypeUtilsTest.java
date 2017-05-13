package at.doml.restinfo.type;

import at.doml.restinfo.TypeWriter;
import org.junit.Test;
import org.mockito.InOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public final class TypeUtilsTest {
    
    private InOrder inOrderMock;
    private final TypeWriter mockWriter = mock(TypeWriter.class);
    private final List<Consumer<Object>> callOrder = new ArrayList<>();
    
    //
    // TESTS
    //
    @Test
    public void conditionalWriteShouldCallCorrectBeforeAndAfterMethods() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields,
                TypeWriter::afterAllComplexFields
        );
    
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields,
                ignored -> false,
                ignored -> {},
                TypeWriter::afterAllComplexFields
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteShouldCallOnConditionMethodOnTrueCondition() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields,
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::afterAllComplexFields
        );
    
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields,
                ignored -> true,
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::afterAllComplexFields
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteShouldCallMethodToCheckCondition() {
        CallOrderInfo callOrderInfo = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields
        );
    
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                ignored -> {},
                writer -> {
                    writer.beforeAllComplexFields();
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
                TypeWriter::beforeAllComplexFields,
                TypeWriter::writeBeforeArrayElementType,
                TypeWriter::afterAllComplexFields
        );
    
        this.initializeOrderObject(callOrderInfo);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields,
                writer -> {
                    writer.writeBeforeArrayElementType();
                    return false;
                },
                ignored -> fail("this was not supposed to be called"), // should not be called
                TypeWriter::afterAllComplexFields
        );
        
        this.assertMethodCallOrder(callOrderInfo);
    }
    
    @Test
    public void conditionalWriteWithTypeShouldCallWriteMethodOnGivenType() {
        CallOrderInfo typeWriterCallOrder1 = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::beforeAllComplexFields,
                TypeWriter::writeBeforeArrayElementType
        );
        Type mockType = mock(Type.class);
        CallOrderInfo typeCallOrder = this.defineRequiredCallOrder(
                mockType,
                Type::write
        );
        CallOrderInfo typeWriterCallOrder2 = this.defineRequiredCallOrder(
                this.mockWriter,
                TypeWriter::afterAllComplexFields
        );
    
        this.initializeOrderObject(typeWriterCallOrder1, typeCallOrder, typeWriterCallOrder2);
        
        TypeUtils.conditionalWrite(
                this.mockWriter,
                mockType,
                TypeWriter::beforeAllComplexFields,
                writer -> {
                    writer.writeBeforeArrayElementType();
                    return true;
                },
                TypeWriter::afterAllComplexFields
        );
    
        this.assertMethodCallOrder(typeWriterCallOrder1, typeCallOrder, typeWriterCallOrder2);
    }
    
    //
    // PRIVATE CLASSES
    //
    private static final class CallOrderInfo {
        private final Object object;
        private final int calls;
        
        private CallOrderInfo(Object object, int calls) {
            this.object = object;
            this.calls = calls;
        }
    }
    
    //
    // HELPER METHODS
    //
    @SafeVarargs
    @SuppressWarnings("unchecked")
    private final <T> CallOrderInfo defineRequiredCallOrder(T object, Consumer<T>... methodCalls) {
        for (Consumer<T> methodCall : methodCalls) {
            this.callOrder.add((Consumer<Object>) methodCall);
            methodCall.accept(doNothing().when(object));
        }
        
        return new CallOrderInfo(object, methodCalls.length);
    }
    
    @SafeVarargs
    @SuppressWarnings("unchecked")
    private final <T> CallOrderInfo defineRequiredCallOrder(T object, BiConsumer<T, TypeWriter>... methodCalls) {
        for (BiConsumer<T, TypeWriter> methodCall : methodCalls) {
            Consumer<T> noArgsCaller = (t) -> methodCall.accept(t, this.mockWriter);
            this.callOrder.add((Consumer<Object>) noArgsCaller);
            noArgsCaller.accept(doNothing().when(object));
        }
        
        return new CallOrderInfo(object, methodCalls.length);
    }
    
    private void assertMethodCallOrder(CallOrderInfo... callOrderInfos) {
        Object[] objectsToVerify = createObjectsToVerify(callOrderInfos);
        
        if (objectsToVerify.length != this.callOrder.size()) {
            fail("number of objects and methods to verify is not same:\nobjects: "
                    + objectsToVerify.length + ", methods: " + this.callOrder.size());
        }
        
        int i = 0;
        for (Consumer<Object> methodCall : this.callOrder) {
            methodCall.accept(this.inOrderMock.verify(objectsToVerify[i]));
            i += 1;
        }
        
        this.inOrderMock.verifyNoMoreInteractions();
    }
    
    private void initializeOrderObject(CallOrderInfo... callOrderInfos) {
        this.inOrderMock = inOrder(
                Arrays.stream(callOrderInfos)
                        .map(i -> i.object)
                        .toArray(Object[]::new)
        );
    }
    
    private static Object[] createObjectsToVerify(CallOrderInfo... callOrderInfos) {
        int totalSize = Arrays.stream(callOrderInfos)
                .mapToInt(i -> i.calls)
                .sum();
        int index = 0;
        
        Object[] array = new Object[totalSize];
        
        for (CallOrderInfo callOrderInfo : callOrderInfos) {
            for (int i = 0; i < callOrderInfo.calls; i++) {
                array[index] = callOrderInfo.object;
                index += 1;
            }
        }
        
        return array;
    }
}
