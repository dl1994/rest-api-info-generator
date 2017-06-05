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
        assertNoInfo("model attributes", controllerInfo.getModelAttributesTypeTree());
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
