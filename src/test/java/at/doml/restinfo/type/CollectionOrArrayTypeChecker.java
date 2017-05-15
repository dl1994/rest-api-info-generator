package at.doml.restinfo.type;

final class CollectionOrArrayTypeChecker<T extends CollectionOrArrayType> extends TypeTreeChecker<T> {
    
    private final TypeTreeChecker<?> elementChecker;
    
    CollectionOrArrayTypeChecker(T expectedType, TypeTreeChecker<?> elementChecker) {
        super(expectedType);
        this.elementChecker = elementChecker;
    }
    
    @Override
    void additionalAssertions(T expectedType, T actualType) {
        this.elementChecker.assertType(actualType.elementType);
    }
}
