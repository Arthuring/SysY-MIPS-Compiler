package mid.ircode;

import front.TableEntry;

public class PrintInt extends InstructionLinkNode {
    private final Operand value;

    public PrintInt(Operand value) {
        this.value = value;
    }

    public Operand getValue() {
        return value;
    }

    @Override
    public String toIr() {
        return "\t" + "call void @putint(" +
                ((value instanceof Immediate) ? "i32" :
                        TableEntry.TO_IR.get(((TableEntry) value).valueType))
                + " " + value.toNameIr() + " )";
    }
}
