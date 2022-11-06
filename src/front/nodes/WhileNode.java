package front.nodes;

public class WhileNode implements StmtNode {
    private ExprNode cond;
    private final BlockNode whileStmt;

    public WhileNode(ExprNode cond, BlockNode whileStmt) {
        this.cond = cond;
        this.whileStmt = whileStmt;
    }

    public ExprNode cond() {
        return cond;
    }

    public BlockNode whileStmt() {
        return whileStmt;
    }

    public void setCond(ExprNode cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        return "WhileNode{\n" +
                "cond=" + cond +
                ", \nwhileStmt=" + whileStmt +
                "\n}";
    }
}
