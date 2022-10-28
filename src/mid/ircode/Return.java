package mid.ircode;

public class Return extends InstructionLinkNode {
    private final Operand returnValue;

    public Return(Operand operand) {
        this.returnValue = operand;
    }

    public Operand getReturnValue() {
        return returnValue;
    }
}
