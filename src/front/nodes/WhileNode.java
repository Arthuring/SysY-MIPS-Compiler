package front.nodes;

public class WhileNode implements StmtNode {
    private final ExprNode cond;
    private final StmtNode whileStmt;

    public WhileNode(ExprNode cond, StmtNode whileStmt) {
        this.cond = cond;
        this.whileStmt = whileStmt;
    }

    public ExprNode cond() {
        return cond;
    }

    public StmtNode whileStmt() {
        return whileStmt;
    }
}
