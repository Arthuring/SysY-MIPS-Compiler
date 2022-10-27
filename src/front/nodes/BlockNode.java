package front.nodes;

import front.FuncEntry;
import front.SymbolTable;

import java.util.List;

public class BlockNode implements StmtNode {
    private final List<BlockItemNode> blockItemNodes;
    private final BlockType type;
    private SymbolTable symbolTable = null;
    private FuncEntry funcEntry = null;
    private int depth = 0;

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

    public FuncEntry getFuncEntry() {
        return funcEntry;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setFuncEntry(FuncEntry funcEntry) {
        this.funcEntry = funcEntry;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
