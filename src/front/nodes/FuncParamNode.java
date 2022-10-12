package front.nodes;

import front.CompileUnit;

import java.util.List;

public class FuncParamNode implements SyntaxNode {
    private final CompileUnit.Type type;
    private final String ident;
    private final int line;
    private final List<ExprNode> dimension;

    public FuncParamNode(CompileUnit.Type type, String name, int line, List<ExprNode> dimension) {
        this.type = type;
        this.ident = name;
        this.line = line;
        this.dimension = dimension;
    }

    public CompileUnit.Type type() {
        return type;
    }

    public int line() {
        return line;
    }

    public List<ExprNode> dimension() {
        return dimension;
    }

    public String ident() {
        return ident;
    }

    @Override
    public String toString() {
        return "FuncParamNode{\n" +
                "type=" + type +
                ",\n ident='" + ident + '\'' +
                ",\n line=" + line +
                ",\n dimension=" + dimension +
                "\n}";
    }
}
