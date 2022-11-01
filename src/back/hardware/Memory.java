package back.hardware;

import front.TableEntry;
import front.nodes.NumberNode;

import java.util.HashMap;
import java.util.Map;

public class Memory {
    private final Map<TableEntry, Integer> globalVarMap = new HashMap<>(); //全局变量 - 虚地址
    private int globalOffset = 0;
    private final Map<Integer, Integer> memoryMap = new HashMap<>(); //虚地址 - 值

    public Memory() {

    }

    public static int roundUp(int value, int round) {
        return value % round == 0 ? value : round * (value / round + 1);
    }

    public static int roundDown(int value, int round) {
        return value % round == 0 ? value : round * (value / round);
    }

    public int allocGlobalVar(TableEntry tableEntry) {
        if (globalVarMap.containsKey(tableEntry)) {
            return globalVarMap.get(tableEntry);
        }
        int offset = roundUp(globalOffset, 4);
        globalOffset = roundUp(globalOffset, 4);
        if (tableEntry.refType == TableEntry.RefType.ITEM) {
            memoryMap.put(RF.GP_INIT + offset, ((NumberNode) tableEntry.initValue).number());
            globalOffset += tableEntry.valueType.sizeof();
        } else {
            //TODO : 数组
        }
        tableEntry.setAddress(offset);
        globalVarMap.put(tableEntry, RF.GP_INIT + offset);
        return offset;
    }

    public int loadWord(int offset) {
        return memoryMap.getOrDefault(offset, 0);
    }

    public int getGlobalOffset() {
        return globalOffset;
    }
}
