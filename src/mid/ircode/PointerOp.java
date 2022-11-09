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
                    dst.typeToIr() + ", " +
                    src.typeToIr() + "* " + src.toNameIr();
        } else {
            return "\t" + "store " + dst.typeToIr() + " " + src.toNameIr() + ", "
                    + dst.typeToIr() + "* " + dst.toNameIr();
        }
    }

    public TableEntry getDst() {
        return dst;
    }

    public Operand getSrc() {
        return src;
    }

    public Op getOp() {
        return op;
    }
}
