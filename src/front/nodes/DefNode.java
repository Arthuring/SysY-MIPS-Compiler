package front.nodes;

import front.Token;

import java.util.List;

public class DefNode implements SyntaxNode {
    private final String ident;
    private final int line;
    private final List<ExprNode> dimension;
    private final List<ExprNode> initValues;

    public DefNode(String ident, int line, List<ExprNode> dimension, List<ExprNode> initValues) {
        this.ident = ident;
        this.line = line;
        this.dimension = dimension;
        this.initValues = initValues;
    }

    public List<ExprNode> dimension() {
        return dimension;
    }

    public List<ExprNode> initValues() {
        return initValues;
    }

    public int line() {
        return line;
    }

    public String ident() {
        return ident;
    }

    @Override
    public String toString() {
        return "DefNode{\n" +
                "ident='" + ident + '\'' +
                ",\n line=" + line +
                ",\n dimension=" + dimension +
                ",\n initValues=" + initValues +
                "\n}";
    }
}
