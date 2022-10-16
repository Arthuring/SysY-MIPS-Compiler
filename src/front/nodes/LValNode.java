package front.nodes;

import java.util.List;

public class LValNode extends ExprNode {
    private final String ident;
    private final int line;
    private final List<ExprNode> index;

    public LValNode(String ident, int line, List<ExprNode> index, int dimension) {
        super.dimension = dimension - index.size();
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
