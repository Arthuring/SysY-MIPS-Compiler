package mid.ircode;

import front.CompileUnit;
import front.TableEntry;
import front.nodes.BinaryExpNode;

import java.util.HashMap;
import java.util.Map;

public class BinaryOperator extends InstructionLinkNode {
    public enum Op {
        ADD, SUB, MULT, DIV, MOD
    }

    private static final Map<BinaryExpNode.BinaryOp, Op> OP_2_OP = new HashMap<BinaryExpNode.BinaryOp, Op>() {
        {
            put(BinaryExpNode.BinaryOp.ADD, Op.ADD);
            put(BinaryExpNode.BinaryOp.SUB, Op.SUB);
            put(BinaryExpNode.BinaryOp.MUL, Op.MULT);
            put(BinaryExpNode.BinaryOp.DIV, Op.DIV);
            put(BinaryExpNode.BinaryOp.MOD, Op.MOD);
        }
    };


    private final Op op;
    private final TableEntry dst;
    private final Operand src1;
    private final Operand src2;

    public BinaryOperator(Op op, TableEntry dst, Operand src1, Operand src2) {
        this.op = op;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public BinaryOperator(BinaryExpNode.BinaryOp binaryOp, TableEntry dst, Operand src1, Operand src2) {
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

    public String toIr(){

    }
}
