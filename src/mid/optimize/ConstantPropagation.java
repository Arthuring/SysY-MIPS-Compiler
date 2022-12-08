package mid.optimize;

import front.TableEntry;
import front.nodes.UnaryExpNode;
import mid.IrModule;
import mid.ircode.*;

import java.util.*;

public class ConstantPropagation {
    private static Map<InstructionLinkNode, Value> valueMap;
    private static ReachingDefTable reachingDefTable;
    private static final boolean DBG_MOD = false;
    private static Set<InstructionLinkNode> inInstr = new HashSet<>(); // 目前扫描到的指令的in

    public static IrModule constPro(IrModule irModule) {
        ReachingDefAnalyser.reachingDefAnalysis(irModule);
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            constProForFunc(funcDef);
        }
        return irModule;
    }

    public static void constProForFunc(FuncDef funcDef) {
        valueMap = new HashMap<>();
        reachingDefTable = funcDef.getReachingDefTable();
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            InstructionLinkNode ptr = basicBlock.next();
            while (ptr != basicBlock.getEnd()) {
                if (reachingDefTable.isDefine(ptr)) {
                    valueMap.put(ptr, new Value(Value.ValueType.UNDEF, null));
                }
                ptr = ptr.next();
            }
        }
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
                if (!basicBlock.equals(funcDef.getExitBlock())) {
                    InstructionLinkNode ptr = basicBlock.next();
                    inInstr.clear();
                    inInstr.addAll(reachingDefTable.getIn().get(basicBlock.getLabel()));
                    while (ptr != basicBlock.getEnd()) {
                        if (reachingDefTable.isDefine(ptr)) {
                            Value value;
                            if (ptr instanceof BinaryOperator) {
                                value = calculateValueBinary((BinaryOperator) ptr);
                            } else if (ptr instanceof Call) {
                                value = new Value(Value.ValueType.NAC, null);
                            } else if (ptr instanceof PointerOp &&
                                    ((PointerOp) ptr).getOp() == PointerOp.Op.STORE &&
                                    ((PointerOp) ptr).getDst().refType != TableEntry.RefType.POINTER) {
                                value = calculateValueStore((PointerOp) ptr);
                            } else if (ptr instanceof UnaryOperator) {
                                value = calculateValueUnary((UnaryOperator) ptr);
                            } else if (ptr instanceof Input) {
                                value = new Value(Value.ValueType.NAC, null);
                            } else {
                                assert ptr instanceof PointerOp;
                                value = calculateValueLoad((PointerOp) ptr);
                            }
                            if (!value.equals(valueMap.getOrDefault(ptr, new Value(Value.ValueType.UNDEF, null)))) {
                                changed = true;
                                valueMap.put(ptr, value);
                            }
                            inInstr = Util.cup(Util.sub(inInstr, reachingDefTable.getKillOfInstr(ptr)), ptr);

                        }
                        ptr = ptr.next();
                    }
                }
            }
        }
        while (changed);
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            if (!basicBlock.equals(funcDef.getExitBlock())) {
                replaceConst(basicBlock);
            }
        }

    }

    public static void replaceConst(BasicBlock basicBlock) {
        InstructionLinkNode ptr = basicBlock.next();
        inInstr.clear();
        inInstr.addAll(reachingDefTable.getIn().get(basicBlock.getLabel()));
        while (!ptr.equals(basicBlock.getEnd())) {
            boolean changed = false;
            if (reachingDefTable.isDefine(ptr)) {
                Value value;
                if (ptr instanceof BinaryOperator) {
                    value = valueMap.getOrDefault(ptr, new Value(Value.ValueType.UNDEF, null));
                    if (value.valueType() == Value.ValueType.CONS) {
                        UnaryOperator newInstr = new UnaryOperator(UnaryExpNode.UnaryOp.PLUS,
                                ((BinaryOperator) ptr).getDst(),
                                new Immediate(value.constValue()));
                        replaceInstr(ptr, newInstr);
                        changed = true;
                    }
                } else if (ptr instanceof UnaryOperator) {
                    value = valueMap.getOrDefault(ptr, new Value(Value.ValueType.UNDEF, null));
                    if (value.valueType() == Value.ValueType.CONS) {
                        UnaryOperator newInstr = new UnaryOperator(UnaryExpNode.UnaryOp.PLUS,
                                ((UnaryOperator) ptr).getDst(),
                                new Immediate(value.constValue()));
                        replaceInstr(ptr, newInstr);
                        changed = true;
                    }
                } else {
                    value = valueMap.getOrDefault(ptr, new Value(Value.ValueType.UNDEF, null));
                    if (value.valueType() == Value.ValueType.CONS &&
                            ((PointerOp) ptr).getOp() != PointerOp.Op.STORE) { //TODO: store 是否参与常量传播
                        UnaryOperator newInstr = new UnaryOperator(UnaryExpNode.UnaryOp.PLUS,
                                ((PointerOp) ptr).getDst(),
                                new Immediate(value.constValue()));
                        replaceInstr(ptr, newInstr);
                        changed = true;
                    }
                }

            }
            if (!changed) {
                if (ptr instanceof BinaryOperator) {
                    Operand leftO = ((BinaryOperator) ptr).getSrc1();
                    Operand rightO = ((BinaryOperator) ptr).getSrc2();
                    Value left = calculateValueOperand(((BinaryOperator) ptr).getSrc1());
                    Value right = calculateValueOperand(((BinaryOperator) ptr).getSrc2());
                    if (left.valueType() == Value.ValueType.CONS) {
                        leftO = new Immediate(left.constValue());
                    }
                    if (right.valueType() == Value.ValueType.CONS) {
                        rightO = new Immediate(right.constValue());
                    }
                    replaceInstr(ptr, new BinaryOperator(((BinaryOperator) ptr).getOp(),
                            ((BinaryOperator) ptr).getDst(), leftO, rightO));
                } else if (ptr instanceof UnaryOperator) {
                    Value src = calculateValueOperand(((UnaryOperator) ptr).getSrc());
                    if (src.valueType() == Value.ValueType.CONS) {
                        replaceInstr(ptr, new UnaryOperator(((UnaryOperator) ptr).getOp(), ((UnaryOperator) ptr).getDst(),
                                new Immediate(src.constValue())));
                    }
                } else if (ptr instanceof Call) {
                    List<Operand> newOperand = new ArrayList<>();
                    for (Operand operand : ((Call) ptr).getArgs()) {
                        Value value = calculateValueOperand(operand);
                        if (value.valueType() == Value.ValueType.CONS) {
                            newOperand.add(new Immediate(value.constValue()));
                        } else {
                            newOperand.add(operand);
                        }
                    }
                    ((Call) ptr).setArgs(newOperand);
                } else if (ptr instanceof PointerOp) {
                    if (((PointerOp) ptr).getOp() == PointerOp.Op.LOAD &&
                            (((PointerOp) ptr).getSrc()) instanceof TableEntry &&
                            ((TableEntry) ((PointerOp) ptr).getSrc()).refType == TableEntry.RefType.ITEM) {
                        Value value = calculateValueOperand(((PointerOp) ptr).getSrc());
                        if (value.valueType() == Value.ValueType.CONS) {
                            replaceInstr(ptr, new PointerOp(PointerOp.Op.LOAD, ((PointerOp) ptr).getDst(),
                                    new Immediate(value.constValue())));
                        }
                    }
                } else if (ptr instanceof PrintInt) {
                    Value value = calculateValueOperand(((PrintInt) ptr).getValue());
                    if (value.valueType() == Value.ValueType.CONS) {
                        replaceInstr(ptr, new PrintInt(new Immediate(value.constValue())));
                    }
                } else if (ptr instanceof Return) {
                    Operand returnValue = ((Return) ptr).getReturnValue();
                    if (returnValue != null) {
                        Value value = calculateValueOperand(returnValue);
                        if (value.valueType() == Value.ValueType.CONS) {
                            replaceInstr(ptr, new Return(new Immediate(value.constValue())));
                        }
                    }
                } else if (ptr instanceof Branch) {
                    Value value = calculateValueOperand(((Branch) ptr).getCond());
                    if (value.valueType() == Value.ValueType.CONS) {
                        replaceInstr(ptr,
                                new Branch(new Immediate(value.constValue()), ((Branch) ptr).getLabelTrue(), ((Branch) ptr).getLabelFalse(),
                                        ((Branch) ptr).getBrOp()));
                    }
                }
            }
            if (reachingDefTable.isDefine(ptr)) {
                inInstr = Util.cup(Util.sub(inInstr, reachingDefTable.getKillOfInstr(ptr)), ptr);
            }
            ptr = ptr.next();
        }
    }

    public static Value calculateValueBinary(BinaryOperator binaryOperator) {
        Value left = calculateValueOperand(binaryOperator.getSrc1());
        Value right = calculateValueOperand(binaryOperator.getSrc2());
        return calculateBinary(left, right, binaryOperator.getOp());
    }

    public static Value calculateValueUnary(UnaryOperator unaryOperator) {
        Value src = calculateValueOperand(unaryOperator.getSrc());
        return calculateUnary(src, unaryOperator.getOp());
    }

    public static Value calculateValueStore(PointerOp pointerOp) {
        return calculateValueOperand(pointerOp.getSrc());
    }

    public static Value calculateValueLoad(PointerOp pointerOp) {
        return calculateValueOperand(pointerOp.getSrc());
    }

    public static Value calculateValueOperand(Operand operand) {
        if (operand instanceof Immediate) {
            return new Value(Value.ValueType.CONS, ((Immediate) operand).getValue());
        } else {
            if (((TableEntry) operand).isGlobal || ((TableEntry) operand).isParameter ||
                    ((TableEntry) operand).refType == TableEntry.RefType.ARRAY ||
                    ((TableEntry) operand).refType == TableEntry.RefType.POINTER) {
                return new Value(Value.ValueType.NAC, null);
            }
            Set<InstructionLinkNode> reachedDef = Util.cap(reachingDefTable.getDefine().getOrDefault((TableEntry) operand,
                    new HashSet<>()), inInstr);
            if (reachedDef.size() == 0) {
                if (DBG_MOD) {
                    System.out.println("UNKNOWN:" + (operand).toNameIr());
                }
                return new Value(Value.ValueType.NAC, null);
            } else if (reachedDef.size() == 1) {
                return valueMap.getOrDefault(reachedDef.iterator().next(), new Value(Value.ValueType.UNDEF, null));
            } else {
                Value ans = new Value(Value.ValueType.UNDEF, null);
                for (InstructionLinkNode instr : reachedDef) {
                    ans = merge(ans, valueMap.getOrDefault(instr, new Value(Value.ValueType.UNDEF, null)));
                }
                return ans;
            }
        }
    }


    public static Value merge(Value a, Value b) {
        if (a.valueType() == Value.ValueType.UNDEF) {
            return b;
        } else if (b.valueType() == Value.ValueType.UNDEF) {
            return a;
        } else if (a.valueType() == Value.ValueType.CONS
                && b.valueType() == Value.ValueType.CONS
                && a.constValue().equals(b.constValue())) {
            return a;
        } else {
            return new Value(Value.ValueType.NAC, null);
        }
    }

    public static Value calculateBinary(Value a, Value b, BinaryOperator.Op op) {
        if (a.valueType() == Value.ValueType.NAC || b.valueType() == Value.ValueType.NAC) {
            return new Value(Value.ValueType.NAC, null);
        }
        if (a.valueType() == Value.ValueType.UNDEF || b.valueType() == Value.ValueType.UNDEF) {
            return new Value(Value.ValueType.UNDEF, null);
        }
        int constValue;
        switch (op) {
            case ADD:
                constValue = a.constValue() + b.constValue();
                return new Value(Value.ValueType.CONS, constValue);

            case SUB:
                constValue = a.constValue() - b.constValue();
                return new Value(Value.ValueType.CONS, constValue);

            case MULT:
                constValue = a.constValue() * b.constValue();
                return new Value(Value.ValueType.CONS, constValue);
            case DIV:
                constValue = a.constValue() / b.constValue();
                return new Value(Value.ValueType.CONS, constValue);
            case MOD:
                constValue = a.constValue() % b.constValue();
                return new Value(Value.ValueType.CONS, constValue);
            case LSS:
                constValue = a.constValue() < b.constValue() ? 1 : 0;
                return new Value(Value.ValueType.CONS, constValue);
            case LEQ:
                constValue = a.constValue() <= b.constValue() ? 1 : 0;
                return new Value(Value.ValueType.CONS, constValue);
            case NEQ:
                constValue = !Objects.equals(a.constValue(), b.constValue()) ? 1 : 0;
                return new Value(Value.ValueType.CONS, constValue);
            case EQL:
                constValue = Objects.equals(a.constValue(), b.constValue()) ? 1 : 0;
                return new Value(Value.ValueType.CONS, constValue);
            case GRE:
                constValue = a.constValue() > b.constValue() ? 1 : 0;
                return new Value(Value.ValueType.CONS, constValue);
            case GEQ:
                constValue = a.constValue() >= b.constValue() ? 1 : 0;
                return new Value(Value.ValueType.CONS, constValue);
            default:
                return new Value(Value.ValueType.NAC, null);
        }
    }

    public static Value calculateUnary(Value value, UnaryOperator.Op op) {
        if (value.valueType() == Value.ValueType.NAC || value.valueType() == Value.ValueType.UNDEF) {
            return value;
        }
        int value1;
        switch (op) {
            case NOT:
                value1 = (value.constValue() != 0 ? 0 : 1);
                return new Value(Value.ValueType.CONS, value1);
            case PLUS:
                return value;
            case MINU:
                value1 = (value.constValue() * -1);
                return new Value(Value.ValueType.CONS, value1);
            default:
                return new Value(Value.ValueType.NAC, null);
        }
    }

    public static void replaceInstr(InstructionLinkNode oldInstr, InstructionLinkNode newInstr) {
        newInstr.setPrev(oldInstr.prev());
        newInstr.setNext(oldInstr.next());
        oldInstr.prev().setNext(newInstr);
        oldInstr.next().setPrev(newInstr);
    }

}
