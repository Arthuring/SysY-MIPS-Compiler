package back;

import back.hardware.RF;
import back.instr.Lw;
import back.instr.Sw;
import front.TableEntry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegMap {
    private static final Map<Integer, TableEntry> BUSY_REG_TO_VAR = new HashMap<>();
    private static final Map<TableEntry, Integer> VAR_TO_BUST_REG = new HashMap<>();

    private static final Collection<Integer> availableReg = Collections.unmodifiableList(Arrays.asList(
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25
    ));
    private static final Set<Integer> freeRegList = new HashSet<>(availableReg);
    private static final Set<Integer> lruList = new LinkedHashSet<>();


    /**
     * 分配寄存器，若已经分配过，则返回之前分配的，并更新LRU。
     * 若未分配，则分配一个，若需要分配同时加载初值，则needLoad置位。
     * 副作用：会在mipsObject中新增代码
     */
    public static int allocReg(TableEntry tableEntry, MipsObject mipsObject, boolean needLoad) {
        if (VAR_TO_BUST_REG.containsKey(tableEntry)) {
            //已经被分配了寄存器，更新LRU队列
            lruList.remove(VAR_TO_BUST_REG.get(tableEntry));
            lruList.add(VAR_TO_BUST_REG.get(tableEntry));
            mipsObject.addIrDescription(
                    RF.ID_TO_NAME.get(VAR_TO_BUST_REG.get(tableEntry))
                            + "->" + tableEntry.toNameIr());
            return VAR_TO_BUST_REG.get(tableEntry);
        }
        //未被分配
        if (freeRegList.isEmpty()) {
            //找到即将被替换的寄存器
            Iterator<Integer> iterator = lruList.iterator();
            int allocReg = iterator.next();
            iterator.remove();
            //找到即将被替换的变量
            TableEntry replaced = BUSY_REG_TO_VAR.get(allocReg);
            //被替换的变量写回内存
            saveReplacedVar(allocReg, replaced, mipsObject);
            //清除旧映射
            BUSY_REG_TO_VAR.remove(allocReg);
            VAR_TO_BUST_REG.remove(replaced);

            freeRegList.add(allocReg);
        }

        Iterator<Integer> iterator = freeRegList.iterator();
        int allocReg = iterator.next();
        iterator.remove();
        BUSY_REG_TO_VAR.put(allocReg, tableEntry);
        VAR_TO_BUST_REG.put(tableEntry, allocReg);

        mipsObject.addIrDescription(RF.ID_TO_NAME.get(allocReg) + "->" + tableEntry.toNameIr());
        if (needLoad) {
            mipsObject.addIrDescription(RF.ID_TO_NAME.get(allocReg) + "->" + tableEntry.toNameIr()
                    + " = " + "[" + tableEntry.address + "]");
            loadVar(allocReg, tableEntry, mipsObject);
        }
        //更新LRU
        lruList.add(allocReg);
        return allocReg;

    }

    public static void saveReplacedVar(int rt, TableEntry tableEntry, MipsObject mipsObject) {
        if (tableEntry.isGlobal) {
            mipsObject.addAfter(new Sw(rt, tableEntry.address, RF.GPR.GP));
        } else if (tableEntry.isTemp) {
            mipsObject.addAfter(new Sw(rt, tableEntry.address, RF.GPR.SP));
        } else {
            mipsObject.addAfter(new Sw(rt, tableEntry.address * -1, RF.GPR.FP));
        }
    }

    public static void loadVar(int rt, TableEntry tableEntry, MipsObject mipsObject) {
        if (tableEntry.isGlobal) {
            mipsObject.addAfter(new Lw(rt, tableEntry.address, RF.GPR.GP));
        } else if (tableEntry.isTemp) {
            mipsObject.addAfter(new Lw(rt, tableEntry.address, RF.GPR.SP));
        } else {
            mipsObject.addAfter(new Lw(rt, tableEntry.address * -1, RF.GPR.FP));
        }
    }

    public static void clear(MipsObject mipsObject) {
        for (Map.Entry<TableEntry, Integer> it : VAR_TO_BUST_REG.entrySet()) {
            saveReplacedVar(it.getValue(), it.getKey(), mipsObject);
        }
        VAR_TO_BUST_REG.clear();
        BUSY_REG_TO_VAR.clear();
        lruList.clear();
        freeRegList.addAll(availableReg);
    }

    public static void clearWithoutSave(MipsObject mipsObject) {
        VAR_TO_BUST_REG.clear();
        BUSY_REG_TO_VAR.clear();
        lruList.clear();
        freeRegList.addAll(availableReg);
    }

    public static void clearWithoutSave(List<TableEntry> list) {
        for (TableEntry temp : list) {
            assert (temp.isTemp);
            if (VAR_TO_BUST_REG.containsKey(temp)) {
                int reg = VAR_TO_BUST_REG.get(temp);
                VAR_TO_BUST_REG.remove(temp);
                BUSY_REG_TO_VAR.remove(reg);
                lruList.remove(reg);
                freeRegList.add(reg);
            }
        }
    }

}
