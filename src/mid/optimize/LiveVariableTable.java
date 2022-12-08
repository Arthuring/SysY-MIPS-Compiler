package mid.optimize;

import front.TableEntry;
import mid.ircode.BasicBlock;
import mid.ircode.InstructionLinkNode;

import java.util.*;

public class LiveVariableTable {
    private final Map<String, Set<TableEntry>> in = new HashMap<>();
    private final Map<String, Set<TableEntry>> out = new HashMap<>();
    private final Map<String, Set<TableEntry>> def = new HashMap<>();
    private final Map<String, Set<TableEntry>> use = new HashMap<>();

    public LiveVariableTable(){

    }

    public Map<String, Set<TableEntry>> getDef() {
        return def;
    }

    public Map<String, Set<TableEntry>> getIn() {
        return in;
    }

    public Map<String, Set<TableEntry>> getOut() {
        return out;
    }

    public Map<String, Set<TableEntry>> getUse() {
        return use;
    }

    public void calculateDefUse(BasicBlock basicBlock) {
        in.put(basicBlock.getLabel(), new HashSet<>());
        out.put(basicBlock.getLabel(), new HashSet<>());
        Set<TableEntry> useOfBlock = new HashSet<>();
        Set<TableEntry> defOfBlock = new HashSet<>();
        InstructionLinkNode instr = basicBlock.getFirstInstruction();
        while (!instr.equals(basicBlock.getEnd())) {
            Set<TableEntry> useVar = instr.getUseVar();
            for (TableEntry tableEntry : useVar) {
                if (!tableEntry.isTemp && !tableEntry.isGlobal) {
                    if (!defOfBlock.contains(tableEntry)) {
                        useOfBlock.add(tableEntry);
                    }
                }
            }
            TableEntry defVar = instr.getDefineVar();
            if (defVar != null) {
                if (!defVar.isGlobal && !defVar.isTemp) {
                    if (!useOfBlock.contains(defVar)) {
                        defOfBlock.add(defVar);
                    }
                }
            }
            instr = instr.next();
        }
        def.put(basicBlock.getLabel(), defOfBlock);
        use.put(basicBlock.getLabel(), useOfBlock);
    }

    public void calculateInOut(List<BasicBlock> basicBlocks) {
        boolean change;
        do {
            change = false;
            for (int i = basicBlocks.size() - 1; i >= 0; i--) {
                BasicBlock basicBlock = basicBlocks.get(i);
                Set<TableEntry> newOut = new HashSet<>();
                for (String nextLabel : basicBlock.getNextBlock()) {
                    newOut = Util.cup(newOut, in.getOrDefault(nextLabel, new HashSet<>()));
                }
                Set<TableEntry> newIn = Util.cup(use.get(basicBlock.getLabel()), Util.sub(newOut, def.get(basicBlock.getLabel())));
                if (!in.get(basicBlock.getLabel()).equals(newIn) || !out.get(basicBlock.getLabel()).equals(newOut)) {
                    change = true;
                }
                in.put(basicBlock.getLabel(), newIn);
                out.put(basicBlock.getLabel(), newOut);
            }
        } while (change);
    }
}
