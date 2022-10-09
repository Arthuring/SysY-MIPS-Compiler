package front.nodes;

import front.Token;

import java.util.List;

public class DefNode implements SyntaxNode {
    public String ident;
    public int line;
    public final List<ExprNode> dimension;
    public final List<ExprNode> initValues;

    public DefNode(String ident, int line, List<ExprNode> dimension, List<ExprNode> initValues) {
        this.ident = ident;
        this.dimension = dimension;
        this.initValues = initValues;
    }
}
