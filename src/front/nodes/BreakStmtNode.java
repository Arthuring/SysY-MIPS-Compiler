package front.nodes;

public class BreakStmtNode implements StmtNode {
    private final int line;

    public BreakStmtNode(int line) {
        this.line = line;
    }

    public int line() {
        return line;
    }

    @Override
    public String toString() {
        return "BreakStmtNode{\n" +
                "line=" + line +
                "\n}";
    }
}
