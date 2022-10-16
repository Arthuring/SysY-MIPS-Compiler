package front.nodes;

public class NumberNode extends ExprNode {
    private final int number;

    public NumberNode(int number) {
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
