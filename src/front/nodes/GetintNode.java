package front.nodes;

import java.util.Collections;

public class GetintNode extends FuncCallNode {
    public GetintNode(int line) {
        super("getInt", line, Collections.emptyList());
    }

    @Override
    public String toString() {
        return "getint();";
    }
}
