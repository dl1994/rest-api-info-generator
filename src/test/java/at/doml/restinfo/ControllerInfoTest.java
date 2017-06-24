package at.doml.restinfo;

import at.doml.restinfo.type.SimpleType;
import at.doml.restinfo.type.TypeTreeGenerator;
import at.doml.restinfo.type.TypeTreeStub;
import at.doml.restinfo.type.VisitableType;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import static at.doml.restinfo.MockUtils.requestMapping;
import static at.doml.restinfo.type.TypeTreeStub.complex;
import static at.doml.restinfo.type.TypeTreeStub.field;
import static at.doml.restinfo.type.TypeTreeStub.simple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class ControllerInfoTest {

    //
    // TESTS
    //
    @Test
    public void controllerInfoShouldGenerateNotingForEmptyRequestMapping() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping()
        );

        assertNoInfo("path", controllerInfo.getPath());
        assertNoInfo("request method", controllerInfo.getRequestMethod());
        assertNoInfo("response body type", controllerInfo.getResponseBodyTypeTree());
        assertNoInfo("request body type", controllerInfo.getRequestBodyTypeTree());
        assertNoInfo("path variables", controllerInfo.getPathVariablesTypeTree());
        assertNoInfo("query parameters", controllerInfo.getQueryParametersTypeTree());
    }

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
    public void controllerInfoShouldExtractCorrectPathVariable() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().pathVariable("pathVariable", String.class)
        );

        new TypeTreeStub(controllerInfo.getPathVariablesTypeTree())
                .assertStructure(complex(
                        field("pathVariable", simple(SimpleType.STRING))
                ));
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
    public void controllerInfoShouldExtractCorrectModelAttribute() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().modelAttribute("singleModelAttribute", String.class)
        );

        new TypeTreeStub(controllerInfo.getQueryParametersTypeTree())
                .assertStructure(complex(
                        field("singleModelAttribute", simple(SimpleType.STRING))
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

        new TypeTreeStub(controllerInfo.getQueryParametersTypeTree())
                .assertStructure(complex(
                        field("something", simple(SimpleType.STRING)),
                        field("somethingElse", simple(SimpleType.STRING))
                ));
    }

    @Test
    public void controllerInfoShouldExtractCorrectRequestParam() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().requestParameter("someParam", int.class)
        );

        new TypeTreeStub(controllerInfo.getQueryParametersTypeTree())
                .assertStructure(complex(
                        field("someParam", simple(SimpleType.INT))
                ));
    }

    @Test
    public void controllerInfoShouldExtractCorrectRequestParams() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().requestParameter("param1", boolean.class)
                        .requestParameter("nestedParam", new Object() {
                            @SuppressWarnings("unused")
                            public char param2;
                        }.getClass())
        );

        new TypeTreeStub(controllerInfo.getQueryParametersTypeTree())
                .assertStructure(complex(
                        field("param1", simple(SimpleType.BOOLEAN)),
                        field("param2", simple(SimpleType.CHAR))
                ));
    }

    @Test
    public void controllerInfoShouldExtractCorrectModelAttributesAndRequestParams() {
        ControllerInfo controllerInfo = controllerInfo(
                requestMapping().requestParameter("param1", int.class)
                        .requestParameter("param2", char.class)
                        .requestParameter("nestedParams1", new Object() {
                            @SuppressWarnings("unused")
                            public long param3;
                            @SuppressWarnings("unused")
                            public boolean param4;
                        }.getClass())
                        .requestParameter("nestedParams2", new Object() {
                            @SuppressWarnings("unused")
                            public String param5;
                            @SuppressWarnings("unused")
                            public String param6;
                        }.getClass())
                        .modelAttribute("model1", Integer.class)
                        .modelAttribute("model2", String.class)
                        .modelAttribute("nestedModel1", new Object() {
                            @SuppressWarnings("unused")
                            public Long model3;
                        }.getClass())
                        .modelAttribute("nestedModel2", new Object() {
                            @SuppressWarnings("unused")
                            public Short model4;
                            @SuppressWarnings("unused")
                            public boolean model5;
                            @SuppressWarnings("unused")
                            public long model6;
                        }.getClass())
        );

        new TypeTreeStub(controllerInfo.getQueryParametersTypeTree())
                .assertStructure(complex(
                        field("param1", simple(SimpleType.INT)),
                        field("param2", simple(SimpleType.CHAR)),
                        field("param3", simple(SimpleType.LONG)),
                        field("param4", simple(SimpleType.BOOLEAN)),
                        field("param5", simple(SimpleType.STRING)),
                        field("param6", simple(SimpleType.STRING)),
                        field("model1", simple(SimpleType.BOXED_INT)),
                        field("model2", simple(SimpleType.STRING)),
                        field("model3", simple(SimpleType.BOXED_LONG)),
                        field("model4", simple(SimpleType.BOXED_SHORT)),
                        field("model5", simple(SimpleType.BOOLEAN)),
                        field("model6", simple(SimpleType.LONG))
                ));
    }

    //
    // HELPER METHODS
    //
    private static ControllerInfo controllerInfo(MockUtils.RequestMappingBuilder requestMappingBuilder) {
        return new ControllerInfo(new TypeTreeGenerator(), requestMappingBuilder.build());
    }

    //
    // ASSERTIONS
    //
    private static void assertCorrect(String field, Object expected, Object actual) {
        assertEquals(field + " is incorrect", expected, actual);
    }

    private static void assertNoInfo(String field, Object value) {
        assertNull(field + " should be null", value);
    }

    private static void assertNoResponseBody(VisitableType responseBody) {
        assertNull("no response body is expected", responseBody);
    }
}
