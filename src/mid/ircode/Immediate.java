package mid.ircode;

public class Immediate implements Operand {
    private final int value;

    public Immediate(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
