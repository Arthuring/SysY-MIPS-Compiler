package mid.ircode;

import front.TableEntry;

public class PointerOp extends InstructionLinkNode {
    public enum Op {
        LOAD, STORE
    }

    private final Op op;
    private final Operand src;
    private final TableEntry dst;

    public PointerOp(Op op, TableEntry dst, Operand src) {
        this.op = op;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toIr() {
        if (this.op == Op.LOAD) {
            return "\t" + dst.toNameIr() + " = load " +
                    TableEntry.TO_IR.get(dst.valueType) + ", " +
                    "i32* " + src.toNameIr();
        } else {
            return "\t" + "store " + TableEntry.TO_IR.get(dst.valueType) + " " + src.toNameIr() + ", "
                    + "i32* " + dst.toNameIr();
        }
    }
}
