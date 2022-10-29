package mid.ircode;

import front.TableEntry;
import front.nodes.UnaryExpNode;

import java.util.HashMap;
import java.util.Map;

public class UnaryOperator extends InstructionLinkNode {
    public enum Op {
        PLUS, MINU, NOT
    }

    private static final Map<UnaryOperator.Op, String> OP_TO_IR = new HashMap<UnaryOperator.Op, String>() {
        {
            put(Op.PLUS, "add");
            put(Op.MINU, "sub");
        }
    };

    private static final Map<UnaryExpNode.UnaryOp, Op> OP_2_OP = new HashMap<UnaryExpNode.UnaryOp, Op>() {
        {
            put(UnaryExpNode.UnaryOp.PLUS, Op.PLUS);
            put(UnaryExpNode.UnaryOp.MINU, Op.MINU);
            put(UnaryExpNode.UnaryOp.NOT, Op.NOT);
        }
    };


    private final Op op;
    private final TableEntry dst;
    private final Operand src;

    public UnaryOperator(UnaryExpNode.UnaryOp op, TableEntry dst, Operand src) {
        this.op = OP_2_OP.get(op);
        this.dst = dst;
        this.src = src;
    }

    public TableEntry getDst() {
        return dst;
    }

    public Op getOp() {
        return op;
    }

    public Operand getSrc() {
        return src;
    }

    public String toIr() {
        return "\t" + dst.toNameIr() + " = " + OP_TO_IR.get(op) + " "
                + TableEntry.TO_IR.get(dst.valueType) + " " +
                "0, " +
                src.toNameIr();
    }

}
