package front.nodes;

public class IfNode implements StmtNode {
    private final ExprNode cond;
    private final StmtNode ifStmt;
    private final StmtNode elseStmt;

    public IfNode(ExprNode cond, StmtNode ifStmt, StmtNode elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
    }

    public IfNode(ExprNode cond, StmtNode ifStmt) {
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
