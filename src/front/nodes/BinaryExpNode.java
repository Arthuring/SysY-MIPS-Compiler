package front.nodes;

import exception.CompileExc;
import front.CompileUnit;

import java.util.HashMap;
import java.util.Map;

public class BinaryExpNode implements ExprNode {
    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD,
        LEQ, GEQ, EQL, NEQ,
        AND, OR, LSS, GRE
    }

    private static final Map<CompileUnit.Type, BinaryOp> TYPE_2_OP = new HashMap<CompileUnit.Type, BinaryOp>() {
        {
            put(CompileUnit.Type.PLUS, BinaryOp.ADD);
            put(CompileUnit.Type.MINU, BinaryOp.SUB);
            put(CompileUnit.Type.MULT, BinaryOp.MUL);
            put(CompileUnit.Type.DIV, BinaryOp.DIV);
            put(CompileUnit.Type.MOD, BinaryOp.MOD);
            put(CompileUnit.Type.LEQ, BinaryOp.LEQ);
            put(CompileUnit.Type.GEQ, BinaryOp.GEQ);
            put(CompileUnit.Type.EQL, BinaryOp.EQL);
            put(CompileUnit.Type.NEQ, BinaryOp.NEQ);
            put(CompileUnit.Type.AND, BinaryOp.AND);
            put(CompileUnit.Type.OR, BinaryOp.OR);
            put(CompileUnit.Type.LSS, BinaryOp.LSS);
            put(CompileUnit.Type.GRE, BinaryOp.GRE);
        }
    };

    private final BinaryOp op;
    private final ExprNode left;
    private final ExprNode right;

    public BinaryExpNode(ExprNode left, BinaryOp op, ExprNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public BinaryExpNode(ExprNode left, CompileUnit.Type type, ExprNode right) {
        this.op = TYPE_2_OP.get(type);
        this.left = left;
        this.right = right;
    }

    public BinaryOp op() {
        return op;
    }

    public ExprNode left() {
        return left;
    }

    public ExprNode right() {
        return right;
    }

    @Override
    public String toString() {
        return "BinaryExpNode{\n" +
                "op=" + op +
                ",\n left=" + left +
                ",\n right=" + right +
                "\n}";
    }
}
