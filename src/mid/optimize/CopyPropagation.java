package mid.optimize;

import front.TableEntry;
import mid.IrModule;
import mid.ircode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CopyPropagation {
    public static IrModule copyPropagation(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            copyPropagationFunc(funcDef);
        }
        return irModule;
    }

    public static void copyPropagationFunc(FuncDef funcDef) {
        Map<String, Map<TableEntry, TableEntry>> in = new HashMap<>();
        Map<String, Map<TableEntry, TableEntry>> out = new HashMap<>();
        boolean change;
        do {
            change = false;
            for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
                if (!basicBlock.equals(funcDef.getExitBlock())) {
                    Map<TableEntry, TableEntry> inBlock = new HashMap<>();
                    for (String preLabel : basicBlock.getPrevBlock()) {
                        inBlock = Util.cap(inBlock.entrySet(), out.getOrDefault(preLabel, new HashMap<>()).entrySet())
                                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k2));
                    }
                    if (!in.getOrDefault(basicBlock.getLabel(), new HashMap<>()).equals(inBlock)) {
                        change = true;
                    }
                    in.put(basicBlock.getLabel(), inBlock);
                    Map<TableEntry, TableEntry> outBlock = new HashMap<>(inBlock);
                    InstructionLinkNode instr = basicBlock.getFirstInstruction();
                    while (!instr.equals(basicBlock.getEnd())) {
                        calculateMap(instr, outBlock);
                        instr = instr.next();
                    }
                    if (!out.getOrDefault(basicBlock.getLabel(), new HashMap<>()).equals(outBlock)) {
                        change = true;
                    }
                    out.put(basicBlock.getLabel(), outBlock);
                }
            }
        } while (change);
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            if (!basicBlock.equals(funcDef.getExitBlock())) {
                Map<TableEntry, TableEntry> inBlock = new HashMap<>(in.get(basicBlock.getLabel()));
                InstructionLinkNode ptr = basicBlock.getFirstInstruction();
                while (!ptr.equals(basicBlock.getEnd())) {
                    if (ptr instanceof BinaryOperator) {
                        Operand leftO = ((BinaryOperator) ptr).getSrc1();
                        Operand rightO = ((BinaryOperator) ptr).getSrc2();
                        if (leftO instanceof TableEntry) {
                            leftO = getValue((TableEntry) leftO, inBlock);
                        }
                        if (rightO instanceof TableEntry) {
                            rightO = getValue((TableEntry) rightO, inBlock);
                        }
                        Util.replaceInstr(ptr, new BinaryOperator(((BinaryOperator) ptr).getOp(),
                                ((BinaryOperator) ptr).getDst(), leftO, rightO));
                    } else if (ptr instanceof UnaryOperator) {
                        if (((UnaryOperator) ptr).getSrc() instanceof TableEntry) {
                            TableEntry value = getValue((TableEntry) ((UnaryOperator) ptr).getSrc(), inBlock);
                            Util.replaceInstr(ptr, new UnaryOperator(((UnaryOperator) ptr).getOp(), ((UnaryOperator) ptr).getDst(), value));
                        }

                    } else if (ptr instanceof Call) {
                        List<Operand> newOperand = new ArrayList<>();
                        for (Operand operand : ((Call) ptr).getArgs()) {
                            if (operand instanceof TableEntry) {
                                newOperand.add(getValue((TableEntry) operand, inBlock));
                            } else {
                                newOperand.add(operand);
                            }
                        }
                        ((Call) ptr).setArgs(newOperand);
                    } else if (ptr instanceof PointerOp) {
                        if (((PointerOp) ptr).getOp() == PointerOp.Op.STORE &&
                                (((PointerOp) ptr).getSrc()) instanceof TableEntry &&
                                ((TableEntry) ((PointerOp) ptr).getSrc()).refType == TableEntry.RefType.ITEM) {
                            TableEntry value = getValue((TableEntry) ((PointerOp) ptr).getSrc(), inBlock);
                            Util.replaceInstr(ptr, new PointerOp(PointerOp.Op.STORE, ((PointerOp) ptr).getDst(),
                                    value));
                        }
                    } else if (ptr instanceof PrintInt) {
                        Operand value = getOperand(((PrintInt) ptr).getValue(), inBlock);
                        Util.replaceInstr(ptr, new PrintInt(value));

                    } else if (ptr instanceof Return) {
                        Operand returnValue = ((Return) ptr).getReturnValue();
                        if (returnValue != null) {
                            Operand value = getOperand(returnValue, inBlock);
                            Util.replaceInstr(ptr, new Return(value));
                        }
                    } else if (ptr instanceof Branch) {
                        Operand operand = getOperand(((Branch) ptr).getCond(), inBlock);
                        Util.replaceInstr(ptr,
                                new Branch(operand, ((Branch) ptr).getLabelTrue(), ((Branch) ptr).getLabelFalse(),
                                        ((Branch) ptr).getBrOp()));
                    }
                    calculateMap(ptr, inBlock);
                    ptr = ptr.next();
                }
            }
        }
    }

    public static TableEntry getValue(TableEntry src, Map<TableEntry, TableEntry> map) {
        TableEntry value = src;
        while (map.containsKey(value)) {
            value = map.get(value);
        }
        return value;
    }

    public static Operand getOperand(Operand src, Map<TableEntry, TableEntry> map) {
        if (src instanceof Immediate) {
            return src;
        } else {
            return getValue((TableEntry) src, map);
        }
    }

    public static void calculateMap(InstructionLinkNode instr, Map<TableEntry, TableEntry> outBlock) {
        if (instr.getDefineVar() != null && !instr.getDefineVar().isGlobal &&
                !(instr.getDefineVar().refType == TableEntry.RefType.POINTER)) {
            if (instr instanceof UnaryOperator &&
                    ((UnaryOperator) instr).getOp() == UnaryOperator.Op.PLUS &&
                    ((UnaryOperator) instr).getSrc() instanceof TableEntry) {
                TableEntry value = getValue((TableEntry) ((UnaryOperator) instr).getSrc(), outBlock);
                outBlock.put(((UnaryOperator) instr).getDst(), value);
                //TODO:自映射要在窥孔删掉
            } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.LOAD
                    && ((PointerOp) instr).getSrc() instanceof TableEntry
                    && ((TableEntry) ((PointerOp) instr).getSrc()).refType == TableEntry.RefType.ITEM) {
                TableEntry value = getValue((TableEntry) ((PointerOp) instr).getSrc(), outBlock);
                outBlock.put(((PointerOp) instr).getDst(), value);
            } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.STORE
                    && ((PointerOp) instr).getSrc() instanceof TableEntry) {
                TableEntry value = getValue((TableEntry) ((PointerOp) instr).getSrc(), outBlock);
                outBlock.put(((PointerOp) instr).getDst(), value);
            } else {
                outBlock.remove(instr.getDefineVar());
            }
        }
    }
}
