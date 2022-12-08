package mid.optimize;

import front.TableEntry;
import mid.IrModule;
import mid.ircode.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeadCodeKiller {
    public static IrModule deadCodeInBasicBlock(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
                //deleteDeadCodeSingleBlock(basicBlock);
                deleteCodeBlockPro(basicBlock);
            }
        }
        return irModule;
    }

    public static void deleteDeadCodeSingleBlock(BasicBlock basicBlock) {
        Map<TableEntry, Integer> defUseMap = new HashMap<>();
        InstructionLinkNode inst = basicBlock.getFirstInstruction();
        while (inst != basicBlock.getEnd()) {
            if (inst instanceof PointerOp) {
                if (((PointerOp) inst).getOp() == PointerOp.Op.LOAD) {
                    if (((PointerOp) inst).getDst().isTemp) {
                        defUseMap.put(((PointerOp) inst).getDst(), 0);
                    }
                    countUse(((PointerOp) inst).getSrc(), defUseMap);
                } else {
                    countUse(((PointerOp) inst).getSrc(), defUseMap);
                    countUse(((PointerOp) inst).getDst(), defUseMap);
                }
            } else if (inst instanceof BinaryOperator) {
                if (((BinaryOperator) inst).getDst().isTemp) {
                    defUseMap.put(((BinaryOperator) inst).getDst(), 0);
                }
                countUse(((BinaryOperator) inst).getSrc1(), defUseMap);
                countUse(((BinaryOperator) inst).getSrc2(), defUseMap);
            } else if (inst instanceof UnaryOperator) {
                if (((UnaryOperator) inst).getDst().isTemp) {
                    defUseMap.put(((UnaryOperator) inst).getDst(), 0);
                }
                countUse(((UnaryOperator) inst).getSrc(), defUseMap);
            } else if (inst instanceof Branch) {
                countUse(((Branch) inst).getCond(), defUseMap);
            } else if (inst instanceof ElementPtr) {
                if (((ElementPtr) inst).getDst().isTemp) {
                    defUseMap.put(((ElementPtr) inst).getDst(), 0);
                }
                for (Operand operand : ((ElementPtr) inst).getIndex()) {
                    countUse(operand, defUseMap);
                }
            } else if (inst instanceof Call) {
                if (((Call) inst).getReturnDst() != null && ((Call) inst).getReturnDst().isTemp) {
                    defUseMap.put(((Call) inst).getReturnDst(), 0);
                }
                for (Operand operand : ((Call) inst).getArgs()) {
                    countUse(operand, defUseMap);
                }
            } else if (inst instanceof Input) {
                if (((Input) inst).getDst().isTemp) {
                    defUseMap.put(((Input) inst).getDst(), 0);
                }
            } else if (inst instanceof PrintInt) {
                countUse(((PrintInt) inst).getValue(), defUseMap);
            } else if (inst instanceof Return) {
                if (((Return) inst).getReturnValue() != null) {
                    countUse(((Return) inst).getReturnValue(), defUseMap);
                }
            }
            inst = inst.next();
        }
        inst = basicBlock.getFirstInstruction();
        while (inst != basicBlock.getEnd()) {
            if (inst instanceof PointerOp) {
                if (((PointerOp) inst).getOp() == PointerOp.Op.LOAD) {
                    if (((PointerOp) inst).getDst().isTemp) {
                        if (defUseMap.get(((PointerOp) inst).getDst()) == 0) {
                            Util.removeInstr(inst);
                        }
                    }
                }
            } else if (inst instanceof BinaryOperator) {
                if (((BinaryOperator) inst).getDst().isTemp) {
                    if (defUseMap.get(((BinaryOperator) inst).getDst()) == 0) {
                        Util.removeInstr(inst);
                    }
                }
            } else if (inst instanceof UnaryOperator) {
                if (((UnaryOperator) inst).getDst().isTemp) {
                    if (defUseMap.get(((UnaryOperator) inst).getDst()) == 0) {
                        Util.removeInstr(inst);
                    }
                }
            } else if (inst instanceof ElementPtr) {
                if (((ElementPtr) inst).getDst().isTemp) {
                    if (defUseMap.get(((ElementPtr) inst).getDst()) == 0) {
                        Util.removeInstr(inst);
                    }
                }
            }
            inst = inst.next();
        }
    }

    public static void countUse(Operand operand, Map<TableEntry, Integer> map) {
        if (operand instanceof TableEntry && ((TableEntry) operand).isTemp) {
            if (map.containsKey((TableEntry) operand)) {
                map.merge((TableEntry) operand, 1, Integer::sum);
            }
        }
    }

    public static void deleteCodeBlockPro(BasicBlock basicBlock) {
        Map<TableEntry, Integer> liveVar = new HashMap<>();
        InstructionLinkNode instr = basicBlock.getLastInstruction();
        while (!instr.equals(basicBlock)) {
            Set<TableEntry> instrUse = instr.getUseVar();
            for (TableEntry tableEntry : instrUse) {
                if (tableEntry.isTemp) {
                    liveVar.merge(tableEntry, 1, Integer::sum);
                }
            }
            TableEntry dst = instr.getDefineVar();
            if (!(instr instanceof Call) && !(instr instanceof Input)) {
                if (dst != null && dst.isTemp && (!liveVar.containsKey(dst) || liveVar.get(dst) <= 0)) {
                    Util.removeInstr(instr);
                    for (TableEntry tableEntry : instrUse) {
                        liveVar.merge(tableEntry, -1, Integer::sum);
                    }
                }
            }
            instr = instr.prev();
        }
    }

    public static IrModule killUnreachableBlock(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            Map<String, BasicBlock> label2block = funcDef.getLabel2Block();
            for (int i = 0; i < funcDef.getBasicBlocks().size(); i++) {
                BasicBlock basicBlock = funcDef.getBasicBlocks().get(i);
                InstructionLinkNode instr = basicBlock.getFirstInstruction();
                while (instr != basicBlock.getEnd()) {
                    if (instr instanceof Branch) {
                        if (((Branch) instr).getCond() instanceof Immediate) {
                            if (((Branch) instr).getLabelTrue().equals(((Branch) instr).getLabelFalse())) {
                                Util.replaceInstr(instr, new Jump(((Branch) instr).getLabelTrue()));
                            } else {
                                switch (((Branch) instr).getBrOp()) {
                                    case BEQ:
                                        if (((Immediate) ((Branch) instr).getCond()).getValue() == 0) {
                                            Util.replaceInstr(instr, new Jump(((Branch) instr).getLabelFalse()));
                                            basicBlock.getNextBlock().remove(((Branch) instr).getLabelTrue());
                                            basicBlock.getNextBlock().add(((Branch) instr).getLabelFalse());
                                            label2block.get(((Branch) instr).getLabelTrue()).
                                                    getPrevBlock().remove(basicBlock.getLabel());
                                        } else {
                                            Util.removeInstr(instr);
                                            basicBlock.getNextBlock().remove(((Branch) instr).getLabelFalse());
                                            basicBlock.getNextBlock().add(((Branch) instr).getLabelTrue());
                                            label2block.get(((Branch) instr).getLabelFalse()).
                                                    getPrevBlock().remove(basicBlock.getLabel());
                                            if (i + 1 < funcDef.getBasicBlocks().size() &&
                                                    !funcDef.getBasicBlocks().get(i + 1).getLabel().equals(((Branch) instr).getLabelTrue())) {
                                                basicBlock.addAfter(new Jump(((Branch) instr).getLabelTrue()));
                                            }
                                        }
                                        break;
                                    case BNE:
                                        if (((Immediate) ((Branch) instr).getCond()).getValue() != 0) {
                                            Util.replaceInstr(instr, new Jump(((Branch) instr).getLabelTrue()));
                                            basicBlock.getNextBlock().remove(((Branch) instr).getLabelFalse());
                                            basicBlock.getNextBlock().add(((Branch) instr).getLabelTrue());
                                            label2block.get(((Branch) instr).getLabelFalse()).
                                                    getPrevBlock().remove(basicBlock.getLabel());
                                        } else {
                                            Util.removeInstr(instr);
                                            basicBlock.getNextBlock().remove(((Branch) instr).getLabelTrue());
                                            basicBlock.getNextBlock().add(((Branch) instr).getLabelFalse());
                                            label2block.get(((Branch) instr).getLabelTrue()).
                                                    getPrevBlock().remove(basicBlock.getLabel());
                                            if (i + 1 < funcDef.getBasicBlocks().size() &&
                                                    !funcDef.getBasicBlocks().get(i + 1).getLabel().equals(((Branch) instr).getLabelFalse())) {
                                                basicBlock.addAfter(new Jump(((Branch) instr).getLabelFalse()));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    instr = instr.next();
                }
            }
        }
        irModule = FlowGraphBuilder.mergeBlock(irModule);
        irModule = FlowGraphBuilder.removeUnreachable(irModule);
        irModule = FlowGraphBuilder.mergeBlock(irModule);
        return irModule;
    }

    public static void killDeadCodeFunc(FuncDef funcDef) {
        for (int i = funcDef.getBasicBlocks().size() - 2; i >= 0; i--) {
            BasicBlock basicBlock = funcDef.getBasicBlocks().get(i);
            Set<TableEntry> liveVar = new HashSet<>(funcDef.getLiveVariableTable().getOut().get(basicBlock.getLabel()));
            InstructionLinkNode instr = basicBlock.getLastInstruction();
            while (!instr.equals(basicBlock)) {
                boolean moved = false;
                TableEntry dst = instr.getDefineVar();
                if (!(instr instanceof Call) && !(instr instanceof Input)) {
                    if (dst != null && !dst.isGlobal && !liveVar.contains(dst)) {
                        Util.removeInstr(instr);
                        moved = true;
                    }
                }
                if (!moved) {
                    Set<TableEntry> useVar = instr.getUseVar();
                    for (TableEntry tableEntry : useVar) {
                        if (!tableEntry.isGlobal) {
                            liveVar.add(tableEntry);
                        }
                    }
                }
                instr = instr.prev();
            }
        }
    }
}
