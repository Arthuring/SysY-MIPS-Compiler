package front.nodes;

public class ReturnNode implements StmtNode {
    private final int line;
    private final ExprNode returnExpr;

    public ReturnNode(int line, ExprNode returnExpr) {
        this.line = line;
        this.returnExpr = returnExpr;
    }

    public int line() {
        return line;
    }

    public ExprNode returnExpr() {
        return returnExpr;
    }

    @Override
    public String toString() {
        return "ReturnNode{\n" +
                "line=" + line +
                ",\n returnExpr=" + returnExpr +
                "\n}";
    }
}
