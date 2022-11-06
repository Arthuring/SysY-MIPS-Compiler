package mid.ircode;

import back.hardware.Memory;
import front.FuncEntry;
import front.SymbolTable;
import front.TableEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

public class FuncDef extends InstructionLinkNode {
    private final FuncEntry funcEntry;
    private final List<BasicBlock> basicBlocks = new ArrayList<>();
    private final HashSet<TableEntry> allLocalVars = new HashSet<>();
    private Integer space = null;

    public FuncDef(FuncEntry funcEntry) {
        this.funcEntry = funcEntry;
        for (TableEntry tableEntry : funcEntry.args()) {
            tableEntry.setDefined(true);
        }
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

    public HashSet<TableEntry> getAllVars() {
        return allLocalVars;
    }

    public void addLocalVar(SymbolTable symbolTable) {
        allLocalVars.addAll(symbolTable.getVarSymbols().values());
    }

    public int calculateStackSpace() {
        if (space == null) {
            space = 0;
            HashSet<TableEntry> allocatedVars = new HashSet<>();
            for (TableEntry tableEntry : funcEntry.args()) {
                tableEntry.setAddress(Memory.roundUp(space, 4));
                space = Memory.roundUp(space, 4);
                space += tableEntry.sizeof();
                allocatedVars.add(tableEntry);
            }
            for (TableEntry tableEntry : allLocalVars) {
                if (!allocatedVars.contains(tableEntry)) {
                    tableEntry.setAddress(Memory.roundUp(space, 4));
                    space = Memory.roundUp(space, 4);
                    space += tableEntry.sizeof();
                }
            }
            space = Memory.roundUp(space, 4);
        }
        return space;
    }
}
