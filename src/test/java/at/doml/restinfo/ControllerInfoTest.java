package at.doml.restinfo;

import at.doml.restinfo.type.SimpleType;
import at.doml.restinfo.type.TypeTreeGenerator;
import at.doml.restinfo.type.TypeTreeStub;
import at.doml.restinfo.type.VisitableType;
import org.junit.Test;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static at.doml.restinfo.type.TypeTreeStub.complex;
import static at.doml.restinfo.type.TypeTreeStub.field;
import static at.doml.restinfo.type.TypeTreeStub.simple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ControllerInfoTest {

    //
    // TESTS
    //
    @Test
    public void controllerInfoShouldExtractCorrectPath() {
        String expectedPath = "/some/path";
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().path(expectedPath)
        );

        assertCorrect("path", expectedPath, controllerInfo.getPath());
    }

    @Test
    public void controllerInfoShouldExtractCorrectRequestMethod() {
        String expectedMethod = "GET";
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().requestMethod(RequestMethod.GET)
        );

        assertCorrect("request method", expectedMethod, controllerInfo.getRequestMethod());
    }

    @Test
    public void controllerInfoShouldExtractCorrectRequestBody() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().requestBody(String.class)
        );

        assertCorrect("request body type", SimpleType.STRING, controllerInfo.getRequestBodyTypeTree());
    }

    @Test
    public void controllerInfoShouldExtractCorrectResponseBody() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().responseBody(String.class)
        );

        assertCorrect("response body type", SimpleType.STRING, controllerInfo.getResponseBodyTypeTree());
    }

    @Test
    public void controllerInfoShouldTreatVoidAsNoResponseBody() {
        ControllerInfo controllerInfo1 = controllerInfo(
                requestMapping().responseBody(void.class)
        );
        ControllerInfo controllerInfo2 = controllerInfo(
                requestMapping().responseBody(Void.class)
        );

        assertNoResponseBody(controllerInfo1.getResponseBodyTypeTree());
        assertNoResponseBody(controllerInfo2.getResponseBodyTypeTree());
    }

    @Test
    public void controllerInfoShouldExtractCorrectPathVariables() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().pathVariable("someVariable", String.class)
                        .pathVariable("otherVariable", Integer.class)
                        .pathVariable("complexVariable", new Object() {
                            @SuppressWarnings("unused")
                            public int aField;
                        }.getClass())
        );

        new TypeTreeStub(controllerInfo.getPathVariablesTypeTree())
                .assertStructure(complex(
                        field("someVariable", simple(SimpleType.STRING)),
                        field("otherVariable", simple(SimpleType.BOXED_INT)),
                        field("aField", simple(SimpleType.INT))
                ));
    }

    @Test
    public void controllerInfoShouldExtractCorrectModelAttributes() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().modelAttribute("something", String.class)
                        .modelAttribute("complex", new Object() {
                            @SuppressWarnings("unused")
                            public String somethingElse;
                        }.getClass())
        );

        new TypeTreeStub(controllerInfo.getModelAttributesTypeTree())
                .assertStructure(complex(
                        field("something", simple(SimpleType.STRING)),
                        field("somethingElse", simple(SimpleType.STRING))
                ));
    }

    //
    // PRIVATE CLASSES
    //
    private static final class RequestMappingBuilder {

        private String path;
        private RequestMethod requestMethod;
        private Type requestBody;
        private Class<?> responseBody = void.class;
        private final Map<String, Type> pathVariables = new HashMap<>();
        private final Map<String, Type> modelAttributes = new HashMap<>();

        private RequestMappingBuilder path(String path) {
            this.path = path;
            return this;
        }

        private RequestMappingBuilder requestMethod(RequestMethod requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        private RequestMappingBuilder requestBody(Type requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        private RequestMappingBuilder responseBody(Class<?> responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        private RequestMappingBuilder pathVariable(String name, Type type) {
            this.pathVariables.put(name, type);
            return this;
        }

        private RequestMappingBuilder modelAttribute(String name, Type type) {
            this.modelAttributes.put(name, type);
            return this;
        }

        private Map.Entry<RequestMappingInfo, HandlerMethod> build() {
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

    //
    // HELPER METHODS
    //
    private static RequestMappingBuilder requestMapping() {
        return new RequestMappingBuilder();
    }

    private static ControllerInfo controllerInfo(RequestMappingBuilder requestMappingBuilder) {
        return new ControllerInfo(new TypeTreeGenerator(), requestMappingBuilder.build());
    }

    //
    // ASSERTIONS
    //
    private static void assertCorrect(String field, Object expected, Object actual) {
        assertEquals(field + " is incorrect", expected, actual);
    }

    private static void assertNoResponseBody(VisitableType responseBody) {
        assertNull("no response body is expected", responseBody);
    }
}
