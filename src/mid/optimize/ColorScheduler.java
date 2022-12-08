package mid.optimize;

import front.TableEntry;
import mid.IrModule;
import mid.ircode.BasicBlock;
import mid.ircode.FuncDef;
import mid.ircode.InstructionLinkNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColorScheduler {
    public static IrModule colorScheduleReg(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            LiveVariableAnalyser.liveVariableAnalyseFunc(funcDef);
        }
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            DeadCodeKiller.killDeadCodeFunc(funcDef);
        }
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            Map<TableEntry, Set<TableEntry>> conflictMap = conflictMapBuilder(funcDef);
            funcDef.setColorResult(genColorResult(conflictMap));
        }
        return irModule;
    }

    public static Map<TableEntry, Set<TableEntry>> conflictMapBuilder(FuncDef funcDef) {
        Map<TableEntry, Set<TableEntry>> conflictMap = new HashMap<>();
        LiveVariableTable liveVariableTable = funcDef.getLiveVariableTable();
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            Set<TableEntry> liveVar =
                    new HashSet<>(liveVariableTable.getOut().get(basicBlock.getLabel()));
            InstructionLinkNode instr = basicBlock.getLastInstruction();
            while (!instr.equals(basicBlock)) {
                for (TableEntry tableEntry : instr.getUseVar()) {
                    if (!tableEntry.isGlobal && !tableEntry.isTemp) {
                        liveVar.add(tableEntry);
                    }
                }
                TableEntry dst = instr.getDefineVar();
                if (dst != null && !dst.isTemp && !dst.isGlobal) {
                    if (!conflictMap.containsKey(dst)) {
                        conflictMap.put(dst, new HashSet<>());
                    }
                    liveVar.remove(dst);
                    conflictMap.get(dst).addAll(liveVar);
                    for (TableEntry tableEntry : liveVar) {
                        if (!conflictMap.containsKey(tableEntry)) {
                            conflictMap.put(tableEntry, new HashSet<>());
                        }
                        conflictMap.get(tableEntry).add(dst);
                    }
                }
                instr = instr.prev();
            }
        }
        Set<TableEntry> args = new HashSet<>(funcDef.getFuncEntry().args());
        args.removeIf(it -> it.refType != TableEntry.RefType.ITEM);
        for (TableEntry tableEntry : args) {
            if (!conflictMap.containsKey(tableEntry)) {
                conflictMap.put(tableEntry, new HashSet<>());
            }
            conflictMap.get(tableEntry).addAll(Util.sub(args, tableEntry));
        }
        return conflictMap;
    }

    public static ColorResult genColorResult(Map<TableEntry, Set<TableEntry>> conflictMap) {
        ColorResult colorResult = new ColorResult();
        List<TableEntry> keySet = new ArrayList<>(conflictMap.keySet());
        List<Integer> valueSet = conflictMap.values().stream().map(Set::size).collect(Collectors.toList());
        Map<TableEntry, Integer> varDegree =
                IntStream.range(0, keySet.size())
                        .collect(HashMap::new, (m, i) -> m.put(keySet.get(i), valueSet.get(i)), (m, n) -> {
                        });
        List<Integer> priority = keySet.stream()
                .map(tableEntry -> (1 << tableEntry.level) / (varDegree.get(tableEntry) == 0 ? 1 : varDegree.get(tableEntry)))
                .collect(Collectors.toList());
        Map<TableEntry, Integer> varPriority = IntStream.range(0, keySet.size())
                .collect(HashMap::new, (m, i) -> m.put(keySet.get(i), priority.get(i)), (m, n) -> {
                });
        Map<TableEntry, Integer> sortedDegree = varDegree.entrySet().stream().
                sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        Map<TableEntry, Integer> sortedPriority = varPriority.entrySet().stream().
                sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        ArrayList<TableEntry> toAllocate = new ArrayList<>();
        HashSet<TableEntry> allocated = new HashSet<>();
        HashMap<TableEntry, Set<TableEntry>> conflictMapCopy = new HashMap<>(conflictMap);
        while (sortedDegree.size() > 0) {
            Iterator<Map.Entry<TableEntry, Integer>> iterator = sortedDegree.entrySet().iterator();
            Map.Entry<TableEntry, Integer> it = iterator.next();
            if (conflictMapCopy.get(it.getKey()).size() < ColorResult.getAvailableReg().size()) {
                //colorResult.allocVar(it.getKey(), conflictMap.get(it.getKey()));
                toAllocate.add(it.getKey());
                for (TableEntry tableEntry : conflictMapCopy.get(it.getKey())) {
                    conflictMapCopy.get(tableEntry).remove(it.getKey());
                }
                conflictMapCopy.remove(it.getKey());
                sortedPriority.remove(it.getKey());
                iterator.remove();
            } else {
                Iterator<Map.Entry<TableEntry, Integer>> iteratorP = sortedPriority.entrySet().iterator();
                Map.Entry<TableEntry, Integer> itP = iteratorP.next();
                for (TableEntry tableEntry : conflictMap.get(itP.getKey())) {
                    conflictMap.get(tableEntry).remove(itP.getKey());
                }
                for (TableEntry tableEntry : conflictMapCopy.get(itP.getKey())) {
                    conflictMapCopy.get(tableEntry).remove(itP.getKey());
                }
                conflictMap.remove(itP.getKey());
                conflictMapCopy.remove(itP.getKey());
                sortedDegree.remove(itP.getKey());
                iteratorP.remove();
            }
        }
        for (int i = toAllocate.size() - 1; i >= 0; i--) {
            TableEntry tableEntry = toAllocate.get(i);
            colorResult.allocVar(tableEntry, Util.cap(allocated, conflictMap.get(tableEntry)));
            allocated.add(tableEntry);
        }
        // System.out.println(colorResult);
        return colorResult;
    }
}
