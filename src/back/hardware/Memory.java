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
        tableEntry.setAddress(offset);
        globalVarMap.put(tableEntry, RF.GP_INIT + offset);

        if (tableEntry.refType == TableEntry.RefType.ITEM) {
            int value = tableEntry.initValue == null ? 0 : ((NumberNode) tableEntry.initValue).number();
            memoryMap.put(RF.GP_INIT + globalOffset, value);
            globalOffset += tableEntry.valueType.sizeof();
        } else {
            //TODO : 数组
            for (int i = 0; i < tableEntry.sizeof(); i += 4) {
                int value = tableEntry.initValueList == null ? 0 :
                        ((NumberNode) tableEntry.initValueList.get(i / 4)).number();
                memoryMap.put(RF.GP_INIT + globalOffset, value);
                globalOffset += 4;
            }
        }

        return offset;
    }

    public int loadWord(int offset) {
        return memoryMap.getOrDefault(RF.GP_INIT + offset, 0);
    }

    public int getGlobalOffset() {
        return globalOffset;
    }
}
