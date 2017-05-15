package at.doml.restinfo.type;

import at.doml.restinfo.TypeVisitor;
import org.mockito.InOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

abstract class AbstractTypeVisitorMethodCallOrderTest {
    
    final TypeVisitor mockVisitor = mock(TypeVisitor.class);
    
    private InOrder inOrderMock;
    private InOrder conditionalOrderMock;
    private final Collection<Consumer<Object>> callOrder = new ArrayList<>();
    private final Collection<Function<TypeVisitor, Boolean>> conditionalOrder = new ArrayList<>();
    
    //
    // STATIC CLASSES
    //
    static final class CallOrderInfo {
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
    final <T> CallOrderInfo defineRequiredCallOrder(T object, Consumer<T>... methodCalls) {
        for (Consumer<T> methodCall : methodCalls) {
            this.callOrder.add((Consumer<Object>) methodCall);
            methodCall.accept(doNothing().when(object));
        }
        
        return new CallOrderInfo(object, methodCalls.length);
    }
    
    @SafeVarargs
    @SuppressWarnings("unchecked")
    final <T, U> CallOrderInfo defineRequiredCallOrderWithValue(T object, U value, BiConsumer<T, U>... methodCalls) {
        for (BiConsumer<T, U> methodCall : methodCalls) {
            Consumer<T> noArgsCaller = t -> methodCall.accept(t, value);
            this.callOrder.add((Consumer<Object>) noArgsCaller);
            noArgsCaller.accept(doNothing().when(object));
        }
        
        return new CallOrderInfo(object, methodCalls.length);
    }
    
    @SafeVarargs
    @SuppressWarnings("unchecked")
    final CallOrderInfo defineConditionalCallOrder(Function<TypeVisitor, Boolean>... conditionalCalls) {
        for (Function<TypeVisitor, Boolean> conditionalCall : conditionalCalls) {
            this.conditionalOrder.add(conditionalCall);
            conditionalCall.apply(doReturn(false).when(this.mockVisitor));
        }
        
        return new CallOrderInfo(this.mockVisitor, conditionalCalls.length);
    }
    
    void initializeOrderObject(CallOrderInfo... callOrderInfos) {
        this.inOrderMock = inOrder(
                Arrays.stream(callOrderInfos)
                        .map(i -> i.object)
                        .toArray(Object[]::new)
        );
    }
    
    void initializeConditionalOrderObject(CallOrderInfo... callOrderInfos) {
        this.conditionalOrderMock = inOrder(
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
    
    //
    // ASSERTIONS
    //
    void assertMethodCallOrder(CallOrderInfo... callOrderInfos) {
        assertOrder(
                this.callOrder,
                (methodCall, object) -> methodCall.accept(this.inOrderMock.verify(object)),
                callOrderInfos
        );
    }
    
    void assertConditionalCallOrder(CallOrderInfo... callOrderInfos) {
        assertOrder(
                this.conditionalOrder,
                (conditionalCall, object) ->
                        conditionalCall.apply(this.conditionalOrderMock.verify((TypeVisitor) object)),
                callOrderInfos
        );
    }
    
    private static <T> void assertOrder(Collection<T> orders, BiConsumer<T, Object> action,
                                        CallOrderInfo... callOrderInfos) {
        Object[] objectsToVerify = createObjectsToVerify(callOrderInfos);
        
        if (objectsToVerify.length != orders.size()) {
            fail("number of objects and methods to verify is not same:\nobjects: "
                    + objectsToVerify.length + ", methods: " + orders.size());
        }
        
        int i = 0;
        for (T order : orders) {
            action.accept(order, objectsToVerify[i]);
            i += 1;
        }
    }
}
