package front.nodes;

import java.util.List;

public class BlockNode implements StmtNode {
    private final List<BlockItemNode> blockItemNodes;
    private final BlockType type;

    public enum BlockType {
        FUNC, BRANCH, LOOP, BASIC
    }

    public BlockType type() {
        return type;
    }

    public BlockNode(List<BlockItemNode> blockItemNodes, BlockType type) {
        this.blockItemNodes = blockItemNodes;
        this.type = type;
    }

    public List<BlockItemNode> blockItemNodes() {
        return blockItemNodes;
    }

    @Override
    public String toString() {
        return "BlockNode{\n" +
                "blockItemNodes=" + blockItemNodes +
                "\n}";
    }
}
