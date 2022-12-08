package mid.optimize;

import front.TableEntry;

import java.util.*;
import java.util.stream.Collectors;

public class ColorResult {
    private final Map<Integer, Set<TableEntry>> allocMap = new HashMap<>();
    private static final Set<Integer> availableReg = new LinkedHashSet<Integer>() {
        {
            add(16);
            add(17);
            add(18);
            add(19);
            add(20);
            add(21);
            add(22);
            add(23);
        }
    };
    private final Map<TableEntry, Integer> var2Reg = new HashMap<>();

    public ColorResult() {
        for (Integer i : availableReg) {
            allocMap.put(i, new HashSet<>());
        }
    }

    public void allocVar(TableEntry tableEntry, Set<TableEntry> conflict) {
        Set<Integer> conflictSet = conflict.stream()
                .map(tableEntry1 -> var2Reg.getOrDefault(tableEntry1, 0))
                .collect(Collectors.toSet());
        Set<Integer> available = Util.sub(availableReg, conflictSet);
        if (available.size() > 0) {
            int reg = available.iterator().next();
            allocMap.get(reg).add(tableEntry);
            var2Reg.put(tableEntry, reg);
        }
    }

    public static Set<Integer> getAvailableReg() {
        return availableReg;
    }

    public Map<TableEntry, Integer> getVar2Reg() {
        return var2Reg;
    }

    public Map<Integer, Set<TableEntry>> getAllocMap() {
        return allocMap;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        for (int reg : availableReg) {
            StringJoiner sj2 = new StringJoiner(", ");
            for (TableEntry tableEntry : allocMap.get(reg)) {
                sj2.add(tableEntry.toNameIr());
            }
            sj.add(reg + ":" + sj2);
        }
        return sj.toString();
    }
}
