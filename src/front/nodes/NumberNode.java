package front.nodes;

import front.TableEntry;

public class NumberNode extends ExprNode {
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
}
