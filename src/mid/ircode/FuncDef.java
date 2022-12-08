package mid.ircode;

import back.hardware.Memory;
import front.FuncEntry;
import front.SymbolTable;
import front.TableEntry;
import mid.optimize.ColorResult;
import mid.optimize.LiveVariableTable;
import mid.optimize.ReachingDefTable;

import java.util.*;

public class FuncDef extends InstructionLinkNode {
    private final FuncEntry funcEntry;
    private final Map<Integer, BasicBlock> id2Block = new HashMap<>();
    private final Map<String, BasicBlock> label2Block = new HashMap<>();
    private final List<BasicBlock> basicBlocks = new ArrayList<>();
    private final HashSet<TableEntry> allLocalVars = new HashSet<>();
    private Integer space = null;
    private BasicBlock exitBlock;
    private ReachingDefTable reachingDefTable;
    private LiveVariableTable liveVariableTable;
    private ColorResult colorResult;

    public void setColorResult(ColorResult colorResult) {
        this.colorResult = colorResult;
    }

    public ColorResult getColorResult() {
        return colorResult;
    }

    public void setLiveVariableTable(LiveVariableTable liveVariableTable) {
        this.liveVariableTable = liveVariableTable;
    }

    public LiveVariableTable getLiveVariableTable() {
        return liveVariableTable;
    }

    public void setExitBlock(BasicBlock exitBlock) {
        basicBlocks.add(exitBlock);
        this.exitBlock = exitBlock;
        label2Block.put(exitBlock.getLabel(), exitBlock);
    }

    public BasicBlock getExitBlock() {
        return exitBlock;
    }

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
                space += tableEntry.sizeof();
                space = Memory.roundUp(space, 4);
                tableEntry.setAddress(Memory.roundDown(space - 1, 4));
                allocatedVars.add(tableEntry);
            }
            for (TableEntry tableEntry : allLocalVars) {
                if (!allocatedVars.contains(tableEntry)) {
                    space = Memory.roundUp(space, 4);
                    space += tableEntry.sizeof();
                    tableEntry.setAddress(Memory.roundDown(space - 1, 4));
                }
            }
            space = Memory.roundUp(space, 4);
        }
        return space;
    }

    public Map<Integer, BasicBlock> getId2Block() {
        return id2Block;
    }

    public Map<String, BasicBlock> getLabel2Block() {
        return label2Block;
    }


    public void setReachingDefTable(ReachingDefTable reachingDefTable) {
        this.reachingDefTable = reachingDefTable;
    }

    public ReachingDefTable getReachingDefTable() {
        return reachingDefTable;
    }
}
