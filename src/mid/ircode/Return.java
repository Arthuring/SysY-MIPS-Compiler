package mid.ircode;

import front.TableEntry;

public class Return extends InstructionLinkNode {
    private final Operand returnValue;

    public Return(Operand operand) {
        this.returnValue = operand;
    }

    public Operand getReturnValue() {
        return returnValue;
    }

    @Override
    public String toIr() {
        if (returnValue != null) {
            return "\tret " + ((returnValue instanceof Immediate) ? "i32" :
                    TableEntry.TO_IR.get(((TableEntry) returnValue).valueType))
                    + " " + returnValue.toNameIr();
        } else {
            return "\t ret void";
        }
    }
}
