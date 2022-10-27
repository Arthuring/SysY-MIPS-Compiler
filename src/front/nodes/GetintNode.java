package front.nodes;

import front.SymbolTable;
import front.TableEntry;

import java.util.Collections;

public class GetintNode extends FuncCallNode {
    public GetintNode(int line) {
        super("getInt", line, Collections.emptyList(), TableEntry.ValueType.INT);
    }

    @Override
    public String toString() {
        return "getint();";
    }

    @Override
    public ExprNode simplify(SymbolTable symbolTable) {
        return this;
    }
}
