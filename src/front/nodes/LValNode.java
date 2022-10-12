package front.nodes;

import java.util.List;

public class LValNode implements ExprNode {
    private final String ident;
    private final int line;
    private final List<ExprNode> index;

    public LValNode(String ident, int line, List<ExprNode> index) {
        this.ident = ident;
        this.line = line;
        this.index = index;
    }

    public int line() {
        return line;
    }

    public List<ExprNode> index() {
        return index;
    }

    public String ident() {
        return ident;
    }

    @Override
    public String toString() {
        return "LValNode{\n" +
                "ident='" + ident + '\'' +
                ", \nline=" + line +
                ",\n index=" + index +
                "\n}";
    }
}
