package mid.ircode;

import front.FuncEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FuncDef extends InstructionLinkNode {
    private final FuncEntry funcEntry;
    private final List<BasicBlock> basicBlocks = new ArrayList<>();

    public FuncDef(FuncEntry funcEntry) {
        this.funcEntry = funcEntry;
    }

    public void addBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public FuncEntry getFuncEntry() {
        return funcEntry;
    }

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public String toIr() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(funcEntry.toIr() + " {");
        for (BasicBlock basicBlock : basicBlocks) {
            sj.add(basicBlock.toIr());
        }
        sj.add("}");
        return sj.toString();
    }
}
