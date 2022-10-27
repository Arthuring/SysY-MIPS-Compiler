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
}
