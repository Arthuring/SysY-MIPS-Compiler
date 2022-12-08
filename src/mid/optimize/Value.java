package mid.optimize;

import java.util.Objects;

public class Value {

    enum ValueType {
        UNDEF, NAC, CONS;
    }

    private final Integer constValue;
    private final ValueType valueType;

    public Value(ValueType valueType, Integer constValue) {
        this.valueType = valueType;
        this.constValue = constValue;
    }

    public ValueType valueType() {
        return valueType;
    }

    public Integer constValue() {
        return constValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(constValue, value.constValue) && valueType == value.valueType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constValue, valueType);
    }

    @Override
    public String toString() {
        return "valueType=" + valueType + " constValue=" + constValue;

    }
}