package front.nodes;

public class IfNode implements StmtNode {
    private final ExprNode cond;
    private final BlockNode ifStmt;
    private final BlockNode elseStmt;

    public IfNode(ExprNode cond, BlockNode ifStmt, BlockNode elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
    }

    public IfNode(ExprNode cond, BlockNode ifStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = null;
    }

    public ExprNode cond() {
        return cond;
    }

    public StmtNode ifStmt() {
        return ifStmt;
    }

    public StmtNode elseStmt() {
        return elseStmt;
    }

    @Override
    public String toString() {
        return "IfNode{\n" +
                "cond=" + cond +
                ",\n ifStmt=" + ifStmt +
                ",\n elseStmt=" + elseStmt +
                "\n}";
    }
}
