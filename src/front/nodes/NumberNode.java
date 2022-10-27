package front.nodes;

import front.SymbolTable;
import front.TableEntry;
import mid.ircode.Operand;

public class NumberNode extends ExprNode  {
    private final int number;

    public NumberNode(int number) {
        super.valueType = TableEntry.ValueType.INT;
        super.dimension = 0;
        this.number = number;
    }

    public int number() {
        return number;
    }

    @Override
    public String toString() {
        return "NumberNode{\n" +
                "number=" + number +
                "\n}";
    }

    @Override
    public ExprNode simplify(SymbolTable symbolTable) {
        return this;
    }
}
