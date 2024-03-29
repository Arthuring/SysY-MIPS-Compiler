package front.nodes;

import front.CompileUnit;
import front.SymbolTable;


import java.util.HashMap;
import java.util.Map;

public class UnaryExpNode extends ExprNode {
    public enum UnaryOp {
        PLUS, MINU, NOT
    }

    private static final Map<CompileUnit.Type, UnaryOp> TYPE_2_OP = new HashMap<CompileUnit.Type, UnaryOp>() {
        {
            put(CompileUnit.Type.PLUS, UnaryOp.PLUS);
            put(CompileUnit.Type.MINU, UnaryOp.MINU);
            put(CompileUnit.Type.NOT, UnaryOp.NOT);
        }
    };

    private final UnaryOp op;
    private final ExprNode expNode;


    public UnaryExpNode(CompileUnit.Type type, ExprNode exprNode) {
        super.valueType = exprNode.valueType;
        super.dimension = exprNode.dimension;
        this.op = TYPE_2_OP.get(type);
        this.expNode = exprNode;
    }

    public UnaryExpNode(UnaryOp op, ExprNode exprNode) {
        this.op = op;
        this.expNode = exprNode;
    }

    @Override
    public String toString() {
        return "UnaryExpNode{\n" +
                "op=" + op +
                ",\n expNode=" + expNode +
                "\n}";
    }

    public UnaryOp op() {
        return op;
    }

    public ExprNode expNode() {
        return expNode;
    }

    @Override
    public ExprNode simplify(SymbolTable symbolTable) {
        ExprNode simplifiedExpNode = expNode.simplify(symbolTable);
        if (simplifiedExpNode instanceof NumberNode) {
            switch (op) {
                case NOT:
                    return new NumberNode(((NumberNode) simplifiedExpNode).number() != 0 ? 0 : 1);
                case MINU:
                    return new NumberNode(((NumberNode) simplifiedExpNode).number() * -1);
                case PLUS:
                    return new NumberNode(((NumberNode) simplifiedExpNode).number());
                default:
                    return new UnaryExpNode(op, simplifiedExpNode);
            }
        } else {
            return new UnaryExpNode(op,simplifiedExpNode);
        }
    }
}
