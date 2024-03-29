package mid.ircode;

import front.TableEntry;
import front.nodes.UnaryExpNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnaryOperator extends InstructionLinkNode {
    public enum Op {
        PLUS, MINU, NOT
    }

    private static final Map<UnaryOperator.Op, String> OP_TO_IR = new HashMap<UnaryOperator.Op, String>() {
        {
            put(Op.PLUS, "add");
            put(Op.MINU, "sub");
            put(Op.NOT, "ne");

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
        super();
        this.op = OP_2_OP.get(op);
        this.dst = dst;
        this.src = src;
    }

    public UnaryOperator(Op op, TableEntry dst, Operand src) {
        super();
        this.op = op;
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
        switch (op) {
            case PLUS:
            case MINU:
                return "\t" + dst.toNameIr() + " = " + OP_TO_IR.get(op) + " "
                        + TableEntry.TO_IR.get(dst.valueType) + " " +
                        "0, " +
                        src.toNameIr();
            case NOT:
                return "\t" + dst.toNameIr() + " = icmp " + OP_TO_IR.get(op) + " "
                        + TableEntry.TO_IR.get(dst.valueType) + " " +
                        "0, " +
                        src.toNameIr();

        }
        return "\t" + dst.toNameIr() + " = " + OP_TO_IR.get(op) + " "
                + TableEntry.TO_IR.get(dst.valueType) + " " +
                "0, " +
                src.toNameIr();
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        if (src instanceof TableEntry) {
            useSet.add((TableEntry) src);
        }
        return useSet;
    }

    @Override
    public TableEntry getDefineVar() {
        return dst;
    }
}
