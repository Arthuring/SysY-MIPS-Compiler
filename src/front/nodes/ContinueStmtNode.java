package front.nodes;

public class ContinueStmtNode implements StmtNode {
    private int line;

    public ContinueStmtNode(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "ContinueStmtNode{" +
                "line=" + line +
                '}';
    }
}
