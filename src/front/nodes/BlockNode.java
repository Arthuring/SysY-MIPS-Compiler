package front.nodes;

import java.util.List;

public class BlockNode implements StmtNode {
    private final List<BlockItemNode> blockItemNodes;

    public BlockNode(List<BlockItemNode> blockItemNodes) {
        this.blockItemNodes = blockItemNodes;
    }

    public List<BlockItemNode> blockItemNodes() {
        return blockItemNodes;
    }

    @Override
    public String toString() {
        return "BlockNode{" +
                "blockItemNodes=" + blockItemNodes +
                '}';
    }
}
