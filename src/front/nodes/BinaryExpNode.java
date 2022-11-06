package front.nodes;

import front.CompileUnit;
import front.SymbolTable;
import front.TableEntry;

import java.util.HashMap;
import java.util.Map;

public class BinaryExpNode extends ExprNode {
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
        super.valueType = (left.valueType == TableEntry.ValueType.INT && right.valueType == TableEntry.ValueType.INT) ?
                TableEntry.ValueType.INT : TableEntry.ValueType.VOID;
        super.dimension = Math.max(left.dimension, right.dimension);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public BinaryExpNode(ExprNode left, CompileUnit.Type type, ExprNode right) {
        super.valueType = (left.valueType == TableEntry.ValueType.INT && right.valueType == TableEntry.ValueType.INT) ?
                TableEntry.ValueType.INT : TableEntry.ValueType.VOID;
        super.dimension = Math.max(left.dimension, right.dimension);
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

    @Override
    public ExprNode simplify(SymbolTable symbolTable) {
        ExprNode simplifiedLeft = left.simplify(symbolTable);
        ExprNode simplifiedRight = right.simplify(symbolTable);

        if (simplifiedLeft instanceof NumberNode &&
                simplifiedRight instanceof NumberNode) {
            switch (op) {
                case ADD:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            + ((NumberNode) simplifiedRight).number());
                case SUB:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            - ((NumberNode) simplifiedRight).number());
                case MUL:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            * ((NumberNode) simplifiedRight).number());
                case DIV:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            / ((NumberNode) simplifiedRight).number());
                case MOD:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            % ((NumberNode) simplifiedRight).number());
                case EQL:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            == ((NumberNode) simplifiedRight).number() ? 1 : 0);
                case GEQ:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            >= ((NumberNode) simplifiedRight).number() ? 1 : 0);
                case GRE:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            > ((NumberNode) simplifiedRight).number() ? 1 : 0);
                case LEQ:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            <= ((NumberNode) simplifiedRight).number() ? 1 : 0);
                case LSS:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            < ((NumberNode) simplifiedRight).number() ? 1 : 0);
                case NEQ:
                    return new NumberNode(((NumberNode) simplifiedLeft).number()
                            != ((NumberNode) simplifiedRight).number() ? 1 : 0);
                case OR:
                    return new NumberNode(((NumberNode) simplifiedLeft).number() != 0 ||
                            ((NumberNode) simplifiedRight).number() != 0 ? 1 : 0);
                case AND:
                    return new NumberNode(((NumberNode) simplifiedLeft).number() != 0 &&
                            ((NumberNode) simplifiedRight).number() != 0 ? 1 : 0);
                default:
                    return new BinaryExpNode(simplifiedLeft, op, simplifiedRight);
            }
        } else {
            return new BinaryExpNode(simplifiedLeft, op, simplifiedRight);
        }
    }
}
