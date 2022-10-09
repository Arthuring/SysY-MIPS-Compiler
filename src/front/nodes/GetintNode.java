package front.nodes;

import java.util.Collections;
import java.util.stream.Collector;

public class GetintNode extends FuncCallNode {
    public GetintNode(int line) {
        super("getInt", line, Collections.emptyList());
    }
}
