package front.nodes;

public class AssignNode implements StmtNode {
    private final LValNode lVal;
    private final ExprNode exprNode;

    public AssignNode(LValNode lVal, ExprNode exprNode) {
        this.lVal = lVal;
        this.exprNode = exprNode;
    }

    public ExprNode exprNode() {
        return exprNode;
    }

    public LValNode lVal() {
        return lVal;
    }

    @Override
    public String toString() {
        return "AssignNode{" +
                "lVal=" + lVal +
                ", exprNode=" + exprNode +
                '}';
    }
}
