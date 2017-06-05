package at.doml.restinfo;

import org.mockito.stubbing.OngoingStubbing;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class MockUtils {

    private MockUtils() {}

    //
    // PRIVATE CLASSES
    //
    static final class RequestMappingBuilder {

        private String path;
        private RequestMethod requestMethod;
        private Type requestBody;
        private Class<?> beanType = void.class;
        private Class<?> responseBody = void.class;
        private final Map<String, Type> pathVariables = new HashMap<>();
        private final Map<String, Type> modelAttributes = new HashMap<>();

        RequestMappingBuilder path(String path) {
            this.path = path;
            return this;
        }

        RequestMappingBuilder requestMethod(RequestMethod requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        RequestMappingBuilder requestBody(Type requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        RequestMappingBuilder responseBody(Class<?> responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        RequestMappingBuilder pathVariable(String name, Type type) {
            this.pathVariables.put(name, type);
            return this;
        }

        RequestMappingBuilder modelAttribute(String name, Type type) {
            this.modelAttributes.put(name, type);
            return this;
        }

        RequestMappingBuilder beanType(Class<?> beanType) {
            this.beanType = beanType;
            return this;
        }

        Map.Entry<RequestMappingInfo, HandlerMethod> build() {
            PatternsRequestCondition patternsRequestCondition = this.path == null
                    ? new PatternsRequestCondition()
                    : new PatternsRequestCondition(this.path);
            RequestMethodsRequestCondition requestMethodsRequestCondition = this.requestMethod == null
                    ? new RequestMethodsRequestCondition()
                    : new RequestMethodsRequestCondition(this.requestMethod);
            RequestMappingInfo requestMappingInfo = new RequestMappingInfo(
                    patternsRequestCondition,
                    requestMethodsRequestCondition,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            HandlerMethod handlerMethod = mock(HandlerMethod.class);
            MethodParameter returnType = mock(MethodParameter.class);

            OngoingStubbing<Class<?>> responseBodyStub = when(returnType.getParameterType());
            responseBodyStub.thenReturn(this.responseBody);
            when(returnType.getGenericParameterType()).thenReturn(this.responseBody);

            when(handlerMethod.getReturnType()).thenReturn(returnType);
            MethodParameter[] mockParameters = this.createParameters();
            when(handlerMethod.getMethodParameters()).thenReturn(mockParameters);
            OngoingStubbing<Class<?>> beanTypeStub = when(handlerMethod.getBeanType());
            beanTypeStub.thenReturn(this.beanType);

            return new AbstractMap.SimpleEntry<>(requestMappingInfo, handlerMethod);
        }

        private MethodParameter[] createParameters() {
            List<MethodParameter> parameters = new ArrayList<>();

            if (this.requestBody != null) {
                parameters.add(createParameter("requestBody", this.requestBody, RequestBody.class));
            }

            this.pathVariables.forEach((name, type) -> parameters.add(
                    createParameter(name, type, PathVariable.class)
            ));

            this.modelAttributes.forEach((name, type) -> parameters.add(
                    createParameter(name, type, ModelAttribute.class)
            ));

            return parameters.toArray(new MethodParameter[parameters.size()]);
        }

        private static <A extends Annotation> MethodParameter createParameter(String name, Type type,
                                                                              Class<A> annotation) {
            MethodParameter parameterMock = mock(MethodParameter.class);

            when(parameterMock.getParameterName()).thenReturn(name);
            when(parameterMock.getGenericParameterType()).thenReturn(type);
            when(parameterMock.hasParameterAnnotation(any())).thenReturn(false);
            when(parameterMock.hasParameterAnnotation(annotation)).thenReturn(true);

            return parameterMock;
        }
    }

    static final class RequestMappingHandlerMappingBuilder {

        private final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();

        RequestMappingHandlerMappingBuilder handlerMethod(RequestMappingBuilder handlerMethodBuilder) {
            Map.Entry<RequestMappingInfo, HandlerMethod> entry = handlerMethodBuilder.build();
            this.handlerMethods.put(entry.getKey(), entry.getValue());
            return this;
        }

        RequestMappingHandlerMapping build() {
            RequestMappingHandlerMapping mock = mock(RequestMappingHandlerMapping.class);
            when(mock.getHandlerMethods()).thenReturn(this.handlerMethods);
            return mock;
        }
    }

    //
    // HELPER METHODS
    //
    static RequestMappingBuilder requestMapping() {
        return new RequestMappingBuilder();
    }

    static RequestMappingHandlerMappingBuilder handlerMapping() {
        return new RequestMappingHandlerMappingBuilder();
    }
}
