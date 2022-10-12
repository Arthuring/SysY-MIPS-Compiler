package front.nodes;

public class ContinueStmtNode implements StmtNode {
    private final int line;

    public ContinueStmtNode(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "ContinueStmtNode{\n" +
                "line=" + line +
                "\n}";
    }
}
