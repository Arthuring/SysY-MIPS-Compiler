package mid.ircode;

public class Immediate implements Operand {
    private final int value;

    public Immediate(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toNameIr() {
        return String.valueOf(value);
    }

    @Override
    public String toParamIr() {
        return "i32 " + value;
    }
}
