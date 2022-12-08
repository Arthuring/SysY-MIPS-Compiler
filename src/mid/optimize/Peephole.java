package mid.optimize;

import front.TableEntry;
import mid.IrModule;
import mid.ircode.*;

public class Peephole {
    public static IrModule optimize(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
                InstructionLinkNode inst = basicBlock.next();
                while (!inst.equals(basicBlock.getEnd())) {
                    if (inst instanceof BinaryOperator) {
                        if (((BinaryOperator) inst).getOp() == BinaryOperator.Op.MULT) {
                            Util.replaceInstr(inst, simpleMult((BinaryOperator) inst));
                        } else if (((BinaryOperator) inst).getOp() == BinaryOperator.Op.DIV) {
                            Util.replaceInstr(inst, simpleDiv((BinaryOperator) inst));
                        } else if (((BinaryOperator) inst).getOp() == BinaryOperator.Op.MOD) {
                            Util.replaceInstr(inst, simpleMod((BinaryOperator) inst));
                        }
                    }
                    if (inst instanceof PointerOp &&
                            ((PointerOp) inst).getOp() == PointerOp.Op.LOAD) {
                        if (inst.next() instanceof PointerOp &&
                                ((PointerOp) inst.next()).getOp() == PointerOp.Op.STORE) {
                            if (((PointerOp) inst).getSrc() == ((PointerOp) inst.next()).getDst()) {
                                Util.removeInstr(inst);
                                inst = inst.next();
                                Util.removeInstr(inst);
                            }
                        }
                    }
                    inst = inst.next();
                }
            }
        }
        return irModule;
    }

    public static InstructionLinkNode simpleMult(BinaryOperator instr) {
        if (eqZero(instr.getSrc1()) || eqZero(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(0));
        } else if (eqOne(instr.getSrc1()) && eqOne(instr.getSrc2()) ||
                eqNegOne(instr.getSrc1()) && eqNegOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(1));
        } else if (eqOne(instr.getSrc1())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), instr.getSrc2());
        } else if (eqNegOne(instr.getSrc1())) {
            return new UnaryOperator(UnaryOperator.Op.MINU, instr.getDst(), instr.getSrc2());
        } else if (eqOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), instr.getSrc1());
        } else if (eqNegOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.MINU, instr.getDst(), instr.getSrc1());
        }
        return instr;
    }

    public static InstructionLinkNode simpleDiv(BinaryOperator instr) {
        if (eqZero(instr.getSrc1())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(0));
        } else if (eqOne(instr.getSrc1()) && eqOne(instr.getSrc2()) ||
                eqNegOne(instr.getSrc1()) && eqNegOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(1));
        } else if (eqOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), instr.getSrc1());
        } else if (eqNegOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.MINU, instr.getDst(), instr.getSrc1());
        }
        return instr;
    }

    public static InstructionLinkNode simpleMod(BinaryOperator instr) {
        if (eqZero(instr.getSrc1()) || eqZero(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(0));
        } else if (eqOne(instr.getSrc1()) && eqOne(instr.getSrc2()) ||
                eqNegOne(instr.getSrc1()) && eqNegOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(0));
        } else if (eqOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.PLUS, instr.getDst(), new Immediate(0));
        } else if (eqNegOne(instr.getSrc2())) {
            return new UnaryOperator(UnaryOperator.Op.MINU, instr.getDst(), new Immediate(0));
        }
        return instr;
    }

    public static boolean eqOne(Operand operand) {
        if (operand instanceof TableEntry) {
            return false;
        } else if (operand instanceof Immediate) {
            return ((Immediate) operand).getValue() == 1;
        }
        return false;
    }

    public static boolean eqZero(Operand operand) {
        if (operand instanceof TableEntry) {
            return false;
        } else if (operand instanceof Immediate) {
            return ((Immediate) operand).getValue() == 0;
        }
        return false;
    }

    public static boolean eqNegOne(Operand operand) {
        if (operand instanceof TableEntry) {
            return false;
        } else if (operand instanceof Immediate) {
            return ((Immediate) operand).getValue() == -1;
        }
        return false;
    }
}
