package at.doml.restinfo.type;

/**
 * Class which represents a map in type tree. When <code>TypeVisitor</code> object visits an instance of this
 * class, {@link TypeVisitor#visitBeforeMapKeyType()}, {@link TypeVisitor#shouldVisitMapKeyType()},
 * {@link TypeVisitor#visitAfterMapKeyType()}, {@link TypeVisitor#visitBeforeMapValueType()},
 * {@link TypeVisitor#shouldVisitMapValueType()} and {@link TypeVisitor#visitAfterMapValueType()} methods will be
 * called on the visitor object, in that order. If {@link TypeVisitor#shouldVisitMapKeyType()} returns
 * <code>true</code>, then the visitor will also visit key child element of this map by calling
 * {@link VisitableType#accept(TypeVisitor)} on the child key type, passing the reference to the visitor object. Same
 * goes for {@link TypeVisitor#shouldVisitMapValueType()}: if it returns <code>true</code>, then the visitor will also
 * visit value child element. These child elements will get a reference to the visitor object.
 *
 * @author Domagoj Latečki
 * @version 1.0.0
 * @see TypeVisitor
 * @see TypeTreeGenerator
 */
final class MapType implements VisitableType {

    /**
     * Key element type of this map.
     */
    final VisitableType keyType;
    /**
     * Value element type of this map.
     */
    final VisitableType valueType;

    /**
     * Constructs an object with specified key and value element types.
     *
     * @param keyType   type of the key element.
     * @param valueType type of the value element.
     */
    MapType(VisitableType keyType, VisitableType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        PackageUtils.conditionalVisitForType(
                visitor, this.keyType,
                TypeVisitor::visitBeforeMapKeyType,
                TypeVisitor::shouldVisitMapKeyType,
                TypeVisitor::visitAfterMapKeyType
        );
        PackageUtils.conditionalVisitForType(
                visitor, this.valueType,
                TypeVisitor::visitBeforeMapValueType,
                TypeVisitor::shouldVisitMapValueType,
                TypeVisitor::visitAfterMapValueType
        );
    }
}
