package front.nodes;

import java.util.List;

public class PrintfNode extends FuncCallNode {
    private final String formatString;

    public PrintfNode(int line, String formatString, List<ExprNode> args) {
        super("printf", line, args);
        this.formatString = formatString;
    }

    public String formatString() {
        return formatString;
    }

    @Override
    public String toString() {
        return "PrintfNode{\n" +
                super.toString() +
                "formatString='" + formatString + '\'' +
                "\n}";
    }
}
