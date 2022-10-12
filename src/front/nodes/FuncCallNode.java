package front.nodes;

import java.util.List;

public class FuncCallNode implements ExprNode {
    private final String ident;
    private final int line;
    private final List<ExprNode> args;

    public FuncCallNode(String ident, int line, List<ExprNode> args) {
        this.ident = ident;
        this.line = line;
        this.args = args;
    }

    @Override
    public String toString() {
        return "FuncCallNode{\n" +
                "ident='" + ident + '\'' +
                ",\n line=" + line +
                ",\n args=" + args +
                "\n}";
    }
}
