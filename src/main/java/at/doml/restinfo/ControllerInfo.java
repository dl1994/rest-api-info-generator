package at.doml.restinfo;

import at.doml.restinfo.type.TypeTreeGenerator;
import at.doml.restinfo.type.VisitableType;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ControllerInfo {

    //
    // CONSTANTS
    //
    private static final String NOT_NULL = " must not be null";
    private static final String REQUEST_MAPPING_NOT_NULL = "requestMapping" + NOT_NULL;
    private static final String TYPE_TREE_GENERATOR_NOT_NULL = "typeTreeGenerator" + NOT_NULL;

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    private final String path;
    private final String requestMethod;
    private final VisitableType requestBodyTypeTree;
    private final VisitableType responseBodyTypeTree;
    private final VisitableType queryParametersTypeTree;
    private final VisitableType pathVariablesTypeTree;

    ControllerInfo(TypeTreeGenerator typeTreeGenerator, Map.Entry<RequestMappingInfo, HandlerMethod> requestMapping) {
        Objects.requireNonNull(typeTreeGenerator, TYPE_TREE_GENERATOR_NOT_NULL);
        RequestMappingInfo requestMappingInfo = Objects.requireNonNull(requestMapping, REQUEST_MAPPING_NOT_NULL)
                .getKey();
        HandlerMethod handlerMethod = requestMapping.getValue();

        this.responseBodyTypeTree = generateTree(
                typeTreeGenerator,
                getResponseBodyType(handlerMethod)
        );
        this.requestBodyTypeTree = generateTree(
                typeTreeGenerator,
                getSingleParameterForAnnotation(handlerMethod, RequestBody.class)
        );
        this.pathVariablesTypeTree = generateTree(
                typeTreeGenerator,
                toMap(filterTypesForAnnotation(handlerMethod, PathVariable.class))
        );
        this.queryParametersTypeTree = generateTree(
                typeTreeGenerator,
                toMap(Stream.concat(
                        filterTypesForAnnotation(handlerMethod, ModelAttribute.class),
                        filterTypesForAnnotation(handlerMethod, RequestParam.class)
                ))
        );
        this.path = requestMappingInfo.getPatternsCondition()
                .getPatterns()
                .stream()
                .findFirst()
                .orElse(null);
        this.requestMethod = requestMappingInfo.getMethodsCondition()
                .getMethods()
                .stream()
                .findFirst()
                .map(RequestMethod::name)
                .orElse(null);
    }

    //
    // HELPER METHODS
    //
    private static VisitableType generateTree(TypeTreeGenerator generator, Type type) {
        return Optional.ofNullable(type)
                .map(generator::generateTree)
                .orElse(null);
    }

    private static VisitableType generateTree(TypeTreeGenerator generator, Map<String, Type> types) {
        return Optional.ofNullable(types)
                .map(generator::generateTree)
                .orElse(null);
    }

    private static Map<String, Type> toMap(Stream<MethodParameter> stream) {
        Map<String, Type> map = stream.collect(Collectors.toMap(
                MethodParameter::getParameterName,
                MethodParameter::getGenericParameterType
        ));
        return map.isEmpty() ? null : map;
    }

    private static Type getSingleParameterForAnnotation(HandlerMethod handlerMethod,
                                                        Class<? extends Annotation> annotation) {
        return filterTypesForAnnotation(handlerMethod, annotation)
                .findFirst()
                .map(MethodParameter::getGenericParameterType)
                .orElse(null);
    }

    private static Stream<MethodParameter> filterTypesForAnnotation(HandlerMethod handlerMethod,
                                                                    Class<? extends Annotation> annotation) {
        return Arrays.stream(handlerMethod.getMethodParameters())
                .filter(p -> p.hasParameterAnnotation(annotation))
                .peek(p -> p.initParameterNameDiscovery(new DefaultParameterNameDiscoverer()));
    }

    private static Type getResponseBodyType(HandlerMethod handlerMethod) {
        return Optional.of(handlerMethod.getReturnType())
                .filter(p -> {
                    Class<?> type = p.getParameterType();
                    return !Objects.equals(void.class, type) && !Objects.equals(Void.class, type);
                }).map(MethodParameter::getGenericParameterType)
                .orElse(null);
    }

    //
    // INSTANCE METHODS
    //
    public String getPath() {
        return this.path;
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public VisitableType getRequestBodyTypeTree() {
        return this.requestBodyTypeTree;
    }

    public VisitableType getResponseBodyTypeTree() {
        return this.responseBodyTypeTree;
    }

    public VisitableType getQueryParametersTypeTree() {
        return this.queryParametersTypeTree;
    }

    public VisitableType getPathVariablesTypeTree() {
        return this.pathVariablesTypeTree;
    }
}
