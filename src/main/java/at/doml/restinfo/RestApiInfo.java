package at.doml.restinfo;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class RestApiInfo {

    //
    // CONSTANTS
    //
    private static final String NOT_NULL = " must not be null";
    private static final String SETTINGS_NOT_NULL = "settings" + NOT_NULL;
    private static final String HANDLER_MAPPING_NOT_NULL = "handlerMapping" + NOT_NULL;

    //
    // CONSTRUCTORS AND MEMBER VARIABLES
    //
    private final int numberOfControllers;
    private final Map<String, List<ControllerInfo>> apiSections;

    public RestApiInfo(RequestMappingHandlerMapping handlerMapping) {
        this(RestApiInfoSettings.builder().build(), handlerMapping);
    }

    public RestApiInfo(RestApiInfoSettings settings, RequestMappingHandlerMapping handlerMapping) {
        Objects.requireNonNull(settings, SETTINGS_NOT_NULL);

        this.apiSections = Objects.requireNonNull(handlerMapping, HANDLER_MAPPING_NOT_NULL)
                .getHandlerMethods()
                .entrySet()
                .stream()
                .filter(e -> notExcluded(e, settings))
                .map(e -> {
                    String controllerFullName = e.getValue().getBeanType().getSimpleName();
                    String apiSectionName = settings.apiSectionNamingStrategy.apply(controllerFullName);
                    return new AbstractMap.SimpleEntry<>(apiSectionName, createControllerInfo(e, settings));
                }).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        RestApiInfo::wrapValueInList,
                        RestApiInfo::concatLists
                ));
        this.numberOfControllers = this.apiSections.values()
                .stream()
                .mapToInt(Collection::size)
                .sum();
    }

    //
    // HELPER METHODS
    //
    private static boolean notExcluded(Map.Entry<RequestMappingInfo, HandlerMethod> entry,
                                       RestApiInfoSettings settings) {
        return !settings.excludedControllers.contains(entry.getValue().getBeanType());
    }

    private static ControllerInfo createControllerInfo(Map.Entry<RequestMappingInfo, HandlerMethod> entry,
                                                       RestApiInfoSettings settings) {
        return new ControllerInfo(settings.typeTreeGenerator, entry);
    }

    private static List<ControllerInfo> wrapValueInList(Map.Entry<String, ControllerInfo> entry) {
        List<ControllerInfo> wrapped = new ArrayList<>();
        wrapped.add(entry.getValue());
        return wrapped;
    }

    private static List<ControllerInfo> concatLists(List<ControllerInfo> left, List<ControllerInfo> right) {
        left.addAll(right);
        return left;
    }

    //
    // INSTANCE METHODS
    //
    public void forEachApiSection(Consumer<String> beforeApiSection,
                                  BiConsumer<String, ControllerInfo> onControllerInfo,
                                  Consumer<String> afterApiSection) {
        this.apiSections.forEach((apiSectionName, controllerInfos) -> {
            beforeApiSection.accept(apiSectionName);
            controllerInfos.forEach(controllerInfo -> onControllerInfo.accept(apiSectionName, controllerInfo));
            afterApiSection.accept(apiSectionName);
        });
    }

    public int getNumberOfApiSections() {
        return this.apiSections.size();
    }

    public int getNumberOfControllers() {
        return this.numberOfControllers;
    }
}
