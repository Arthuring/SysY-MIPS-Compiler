package mid.ircode;

public class PrintInt extends InstructionLinkNode {
    private final Operand value;

    public PrintInt(Operand value) {
        this.value = value;
    }

    public Operand getValue() {
        return value;
    }
}
