package at.doml.restinfo.type;

final class MapTypeChecker extends TypeTreeChecker<MapType> {
    
    private final TypeTreeChecker<?> keyChecker;
    private final TypeTreeChecker<?> valueChecker;
    
    MapTypeChecker(MapType expectedType, TypeTreeChecker<?> keyChecker, TypeTreeChecker<?> valueChecker) {
        super(expectedType);
        this.keyChecker = keyChecker;
        this.valueChecker = valueChecker;
    }
    
    @Override
    void additionalAssertions(MapType expectedType, MapType actualType) {
        this.keyChecker.assertType(actualType.keyType);
        this.valueChecker.assertType(actualType.valueType);
    }
}
