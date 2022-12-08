package mid.ircode;

import front.TableEntry;
import front.nodes.BinaryExpNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BinaryOperator extends InstructionLinkNode {
    public enum Op {
        ADD, SUB, MULT, DIV, MOD, EQL, GEQ, GRE, LEQ, LSS, NEQ
    }

    private static final Map<BinaryExpNode.BinaryOp, Op> OP_2_OP = new HashMap<BinaryExpNode.BinaryOp, Op>() {
        {
            put(BinaryExpNode.BinaryOp.ADD, Op.ADD);
            put(BinaryExpNode.BinaryOp.SUB, Op.SUB);
            put(BinaryExpNode.BinaryOp.MUL, Op.MULT);
            put(BinaryExpNode.BinaryOp.DIV, Op.DIV);
            put(BinaryExpNode.BinaryOp.MOD, Op.MOD);
            put(BinaryExpNode.BinaryOp.EQL, Op.EQL);
            put(BinaryExpNode.BinaryOp.GEQ, Op.GEQ);
            put(BinaryExpNode.BinaryOp.GRE, Op.GRE);
            put(BinaryExpNode.BinaryOp.LEQ, Op.LEQ);
            put(BinaryExpNode.BinaryOp.LSS, Op.LSS);
            put(BinaryExpNode.BinaryOp.NEQ, Op.NEQ);
        }
    };

    private static final Map<Op, String> OP_TO_IR = new HashMap<Op, String>() {
        {
            put(Op.ADD, "add");
            put(Op.SUB, "sub");
            put(Op.MULT, "mul");
            put(Op.DIV, "sdiv");
            put(Op.MOD, "srem");
            put(Op.EQL, "eq");
            put(Op.NEQ, "ne");
            put(Op.GRE, "sgt");
            put(Op.GEQ, "sge");
            put(Op.LEQ, "sle");
            put(Op.LSS, "slt");
        }
    };

    private final Op op;
    private final TableEntry dst;
    private final Operand src1;
    private final Operand src2;

    public BinaryOperator(Op op, TableEntry dst, Operand src1, Operand src2) {
        super();
        this.op = op;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public BinaryOperator(BinaryExpNode.BinaryOp binaryOp, TableEntry dst, Operand src1, Operand src2) {
        super();
        this.op = OP_2_OP.get(binaryOp);
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }


    public Op getOp() {
        return op;
    }

    public Operand getSrc1() {
        return src1;
    }

    public Operand getSrc2() {
        return src2;
    }

    public TableEntry getDst() {
        return dst;
    }

    public String toIr() {
        switch (op) {
            case ADD:
            case SUB:
            case MULT:
            case DIV:
            case MOD:
                return "\t" + dst.toNameIr() + " = " + OP_TO_IR.get(op) + " "
                        + TableEntry.TO_IR.get(dst.valueType) + " " +
                        src1.toNameIr() + ", " +
                        src2.toNameIr();
            case NEQ:
            case LSS:
            case LEQ:
            case GRE:
            case GEQ:
            case EQL:
                return "\t" + dst.toNameIr() + " = icmp " + OP_TO_IR.get(op) + " "
                        + TableEntry.TO_IR.get(dst.valueType) + " " +
                        src1.toNameIr() + ", " +
                        src2.toNameIr();
        }
        return "\t" + dst.toNameIr() + " = " + OP_TO_IR.get(op) + " "
                + TableEntry.TO_IR.get(dst.valueType) + " " +
                src1.toNameIr() + ", " +
                src2.toNameIr();

    }

    @Override
    public TableEntry getDefineVar() {
        return this.dst;
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        if (src1 instanceof TableEntry) {
            useSet.add((TableEntry) src1);
        }
        if (src2 instanceof TableEntry) {
            useSet.add((TableEntry) src2);
        }
        return useSet;
    }
}
