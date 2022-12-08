package back;

import back.hardware.Memory;
import back.hardware.RF;
import back.instr.*;
import back.optimize.MultDiv;
import front.FuncEntry;
import front.TableEntry;
import front.nodes.ExprNode;
import front.nodes.NumberNode;
import mid.IrModule;
import mid.ircode.BasicBlock;
import mid.ircode.BinaryOperator;
import mid.ircode.Branch;
import mid.ircode.Call;
import mid.ircode.ElementPtr;
import mid.ircode.FuncDef;
import mid.ircode.Immediate;
import mid.ircode.Input;
import mid.ircode.InstructionLinkNode;
import mid.ircode.Jump;
import mid.ircode.Operand;
import mid.ircode.PointerOp;
import mid.ircode.PrintInt;
import mid.ircode.PrintStr;
import mid.ircode.Return;
import mid.ircode.UnaryOperator;
import mid.optimize.ColorResult;

import java.util.*;

import static mid.ircode.Branch.BrOp.BEQ;
import static mid.ircode.Branch.BrOp.BNE;

/**
 * 将LLVM ir 翻译为 MIPS
 */

public class Translator {
    private final IrModule irModule;
    private final MipsObject mipsObject = new MipsObject();
    private FuncEntry currentFunc = null;
    private FuncDef currentFuncDef = null;
    private BasicBlock currentBasicBlock = null;
    private static final int READ_INT_SYSCALL = 5;
    private static final int PRINT_INT_SYSCALL = 1;
    private static final int PRINT_STR_SYSCALL = 4;

    private static final int READ_REG = RF.GPR.V0;
    private static final int PRINT_REG = RF.GPR.A0;
    private static final int SYSCALL_REG = RF.GPR.V0;
    private static boolean optimizer = false;
    private static boolean betterBranch = true;

    public static void setBetterBranch(boolean betterBranch) {
        Translator.betterBranch = betterBranch;
    }

    public static void setOptimizer(boolean optimizer) {
        Translator.optimizer = optimizer;
    }

    public Translator(IrModule irModule) {
        this.irModule = irModule;
    }

    public IrModule getIrModule() {
        return irModule;
    }

    public MipsObject toMips() {
        globalToMips();
        mipsObject.addAfter(new Move(RF.GPR.FP, RF.GPR.SP));
        mipsObject.addAfter(new J("main"));
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            funcToMips(funcDef);
        }
        return mipsObject;
    }

    public void globalToMips() {
        List<TableEntry> global = irModule.getGlobalVarDefs();
        for (TableEntry var : global) {
            mipsObject.initMem().allocGlobalVar(var);
        }
    }

    public void funcToMips(FuncDef funcDef) {
        currentFunc = funcDef.getFuncEntry();
        currentFuncDef = funcDef;
        int currentStackSize = funcDef.calculateStackSpace();

        //函数入口
        mipsObject.setLabelToSet(funcDef.getFuncEntry().name());
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, -1 * currentStackSize));
        // 申请图着色寄存器
        if (optimizer) {
            RegMap.allocByColorResult(funcDef.getColorResult());
        }
        // 取参数
        int argReg = 4;
        for (TableEntry tableEntry : funcDef.getFuncEntry().args()) {
            if (argReg < 8) {
                if (RegMap.isAllocated(tableEntry)) {
                    mipsObject.addAfter(new Move(allocReg(tableEntry, false), argReg));
                } else {
                    int base = getBase(tableEntry);
                    int offset = getOffset(tableEntry);
                    mipsObject.addAfter(new Sw(argReg, offset, base));
                }
                argReg += 1;
            } else {
                if (RegMap.isAllocated(tableEntry)) {
                    int base = getBase(tableEntry);
                    int offset = getOffset(tableEntry);
                    mipsObject.addAfter(new Lw(allocReg(tableEntry, false), offset, base));
                }
            }
        }
        mipsObject.addIrDescription("basic blocks of func");//函数基本快
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            currentBasicBlock = basicBlock;
            basicBlockToMips(basicBlock);
        }
        mipsObject.addIrDescription("exit func");//exit func
        mipsObject.setLabelToSet(funcDef.getFuncEntry().name() + "_EXIT");
        //flush the occupied regs
        RegMap.clearWithoutSave(mipsObject);
        mipsObject.addIrDescription("free space of local var");//free space of local var
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, currentStackSize));
        //return
        if (funcDef.getFuncEntry().name().equals("main")) {
            mipsObject.addAfter(new Li(RF.GPR.V0, 10));
            mipsObject.addAfter(new Syscall());
        } else {
            mipsObject.addAfter(new Jr(RF.GPR.RA));
        }
    }

    private int tempVarAddr = 0; //相对sp的偏移
    private final Map<TableEntry, Integer> tempRefCounter = new HashMap<>();

    /**
     * 对临时变量引用计数，并分配栈上地址
     */
    public void loadTempVar(BasicBlock basicBlock) {
        tempRefCounter.clear();
        tempVarAddr = 0;
        InstructionLinkNode ptr = basicBlock;
        while (!ptr.equals(basicBlock.getEnd())) {
            ptr = ptr.next();
            if (ptr instanceof BinaryOperator) {
                setTempVarAddr(((BinaryOperator) ptr).getSrc1());
                setTempVarAddr(((BinaryOperator) ptr).getSrc2());
            } else if (ptr instanceof UnaryOperator) {
                setTempVarAddr(((UnaryOperator) ptr).getSrc());
            } else if (ptr instanceof PointerOp) {
                if (((PointerOp) ptr).getOp().equals(PointerOp.Op.STORE)) {
                    setTempVarAddr(((PointerOp) ptr).getSrc());
                    if (((PointerOp) ptr).getDst().refType == TableEntry.RefType.POINTER) {
                        setTempVarAddr(((PointerOp) ptr).getDst());
                    }
                } else {
                    if (((PointerOp) ptr).getSrc() instanceof TableEntry) {
                        TableEntry tableEntry = (TableEntry) ((PointerOp) ptr).getSrc();
                        if (tableEntry.refType == TableEntry.RefType.POINTER) {
                            setTempVarAddr(tableEntry);
                        }
                    }
                }
            } else if (ptr instanceof Call) {
                ((Call) ptr).getArgs().forEach(this::setTempVarAddr);
            } else if (ptr instanceof Return) {
                if (((Return) ptr).getReturnValue() != null) {
                    setTempVarAddr(((Return) ptr).getReturnValue());
                }
            } else if (ptr instanceof Input) {
                setTempVarAddr(((Input) ptr).getDst());
            } else if (ptr instanceof PrintInt) {
                setTempVarAddr(((PrintInt) ptr).getValue());
            } else if (ptr instanceof ElementPtr) {
                for (Operand tableEntry : ((ElementPtr) ptr).getIndex()) {
                    if (tableEntry instanceof TableEntry) {
                        setTempVarAddr(tableEntry);
                    }
                }
            }
        }
    }

    /**
     * 为临时变量分配栈上地址，为相对sp的偏移
     */
    public void setTempVarAddr(Operand operand) {
        if (operand instanceof TableEntry &&
                ((TableEntry) operand).isTemp()) {
            if (!tempRefCounter.containsKey(operand)) {
                ((TableEntry) operand).setAddress(Memory.roundUp(tempVarAddr, 4));
                tempVarAddr = Memory.roundUp(tempVarAddr, 4);
                tempVarAddr += ((TableEntry) operand).valueType.sizeof();
            }
            tempRefCounter.merge((TableEntry) operand, 1, Integer::sum);
        }
    }

    public void basicBlockToMips(BasicBlock basicBlock) {
        loadTempVar(basicBlock);
        mipsObject.setLabelToSet(basicBlock.getLabel());
        //alloc temp space on sp
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, tempVarAddr * -1));
        InstructionLinkNode ptr = basicBlock.getFirstInstruction();
        while (ptr != basicBlock.getEnd()) {
            mipsObject.addIrDescription(ptr.toIr());
            if (ptr instanceof BinaryOperator) {
                binaryOperatorToMips((BinaryOperator) ptr);
            } else if (ptr instanceof UnaryOperator) {
                unaryOperatorToMips((UnaryOperator) ptr);
            } else if (ptr instanceof PointerOp) {
                pointerOpToMips((PointerOp) ptr);
            } else if (ptr instanceof Call) {
                callToMips((Call) ptr);
            } else if (ptr instanceof Return) {
                returnToMips((Return) ptr);
            } else if (ptr instanceof Input) {
                inputToMips((Input) ptr);
            } else if (ptr instanceof PrintInt) {
                printIntToMips((PrintInt) ptr);
            } else if (ptr instanceof PrintStr) {
                printStrToMips((PrintStr) ptr);
            } else if (ptr instanceof Jump) {
                jumpToMips((Jump) ptr);
            } else if (ptr instanceof Branch) {
                branchToMips((Branch) ptr);
            } else if (ptr instanceof ElementPtr) {
                elementPtrToMips((ElementPtr) ptr);
            }
            ptr = ptr.next();
        }
        if (tempVarAddr != 0) {
            getBackTempSpace();
        }
        if (basicBlock.getEndLabel() != null) {
            mipsObject.setLabelToSet(basicBlock.getEndLabel());
        }
    }

    public void elementPtrToMips(ElementPtr elementPtr) {
        mipsObject.setComment(elementPtr.toIr());
        int dst = allocReg(elementPtr.getDst(), false);
        TableEntry baseVar = elementPtr.getBaseVar();
        if (baseVar.refType == TableEntry.RefType.ARRAY) {
            int base = getBase(baseVar);
            int offset = getOffset(baseVar);
            mipsObject.addAfter(new Addiu(dst, base, offset));
        } else if (baseVar.refType == TableEntry.RefType.POINTER) {
            int base = getBase(baseVar);
            int offset = getOffset(baseVar);
            mipsObject.addAfter(new Lw(dst, offset, base));
        }

        List<ExprNode> dim = new ArrayList<>((baseVar.refType == TableEntry.RefType.ARRAY ? baseVar.getDimension() :
                baseVar.getDimension().subList(1, baseVar.getDimension().size())));
        dim.add(new NumberNode(1));
        List<Operand> indexes = new ArrayList<>(elementPtr.getIndex());
        for (int i = 0; i < indexes.size(); i++) {
            Operand index = indexes.get(i);
            if (index instanceof Immediate && ((Immediate) index).getValue() != 0) {
                mipsObject.addAfter(new Addiu(dst, dst, 4 * ((Immediate) index).getValue()
                        * ((NumberNode) dim.get(i)).number()));
            } else if (index instanceof TableEntry) {
                int src = allocReg((TableEntry) index, true);
                if (((NumberNode) dim.get(i)).number() != 1) {
                    if (optimizer) {
                        mipsObject.addAfter(MultDiv.betterMult(RF.GPR.V1, src, ((NumberNode) dim.get(i)).number() * 4));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, ((NumberNode) dim.get(i)).number() * 4));
                        mipsObject.addAfter(new Mul(RF.GPR.V1, RF.GPR.V1, src));
                    }
                    mipsObject.addAfter(new Addu(dst, dst, RF.GPR.V1));
                } else {
                    mipsObject.addAfter(new Sll(RF.GPR.V1, src, 2));
                    mipsObject.addAfter(new Addu(dst, dst, RF.GPR.V1));
                }
            }
        }
    }

    public void getBackTempSpace() {
        RegMap.clearWithoutSave(new ArrayList<>(tempRefCounter.keySet()), mipsObject);
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, tempVarAddr));
        tempRefCounter.clear();
        tempVarAddr = 0;
    }

    public void jumpToMips(Jump jump) {
        getBackTempSpace();
        mipsObject.addAfter(new J(jump.getTarget()));
    }

    public void branchToMips(Branch branch) {
        if (branch.getCond() instanceof Immediate) {
            int value = ((Immediate) branch.getCond()).getValue();
            if (branch.getBrOp() == BEQ && value == 0) {
                getBackTempSpace();
                mipsObject.addAfter(new J(branch.getLabelFalse()));
                return;
            } else if (branch.getBrOp() == BNE && value != 0) {
                getBackTempSpace();
                mipsObject.addAfter(new J(branch.getLabelTrue()));
                return;
            }
            return;
        }
        int rs = allocReg(((TableEntry) branch.getCond()), true);
        getBackTempSpace();
        int index = currentFuncDef.getBasicBlocks().indexOf(currentBasicBlock);
        if (branch.getBrOp() == BEQ) {
            mipsObject.addAfter(new Beq(rs, 0, branch.getLabelFalse()));
            if (index + 1 < currentFuncDef.getBasicBlocks().size() &&
                    !branch.getLabelTrue().equals(currentFuncDef.getBasicBlocks().get(index + 1).getLabel())) {
                mipsObject.addAfter(new J(branch.getLabelTrue()));
            }
        } else {
            mipsObject.addAfter(new Bne(rs, 0, branch.getLabelTrue()));
            if (index + 1 < currentFuncDef.getBasicBlocks().size() &&
                    !branch.getLabelFalse().equals(currentFuncDef.getBasicBlocks().get(index + 1).getLabel())) {
                mipsObject.addAfter(new J(branch.getLabelFalse()));
            }
        }
    }

    public int allocReg(TableEntry tableEntry, boolean needLoad) {
        //needLoad 表示是否需要加载值
        int reg = RegMap.allocReg(tableEntry, mipsObject, needLoad);
        if (!needLoad) {
            RegMap.setRegDirty(reg);
        }
        return reg;
    }

    public void binaryOperatorToMips(BinaryOperator midCode) {
        mipsObject.setComment(midCode.toIr());
        if (midCode.getSrc1() instanceof TableEntry
                && midCode.getSrc2() instanceof TableEntry) {
            //R型
            int rs = allocReg((TableEntry) midCode.getSrc1(), true);
            int rt = allocReg((TableEntry) midCode.getSrc2(), true);
            int rd = allocReg(midCode.getDst(), false);
            switch (midCode.getOp()) {
                case ADD:
                    mipsObject.addAfter(new Addu(rd, rs, rt));
                    break;
                case SUB:
                    mipsObject.addAfter(new Subu(rd, rs, rt));
                    break;
                case MULT:
                    mipsObject.addAfter(new Mul(rd, rs, rt));
                    break;
                case DIV:
                    mipsObject.addAfter(new Div(rs, rt));
                    mipsObject.addAfter(new Mflo(rd));
                    break;
                case MOD:
                    mipsObject.addAfter(new Div(rs, rt));
                    mipsObject.addAfter(new Mfhi(rd));
                    break;
                case GEQ:
                    mipsObject.addAfter(new Sge(rd, rs, rt));
                    break;
                case GRE:
                    mipsObject.addAfter(new Sgt(rd, rs, rt));
                    break;
                case EQL:
                    if (optimizer) {
                        mipsObject.addAfter(new Xor(rd, rs, rt));
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rd));
                        mipsObject.addAfter(new Xori(rd, rd, 1));
                    } else {
                        mipsObject.addAfter(new Seq(rd, rs, rt));
                    }
                    break;
                case NEQ:
                    if (optimizer) {
                        mipsObject.addAfter(new Xor(rd, rs, rt));
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rd));
                    } else {
                        mipsObject.addAfter(new Sne(rd, rs, rt));
                    }
                    break;
                case LEQ:
                    mipsObject.addAfter(new Sle(rd, rs, rt));
                    break;
                case LSS:
                    mipsObject.addAfter(new Slt(rd, rs, rt));
                    break;
            }
        } else if (midCode.getSrc1() instanceof Immediate
                && midCode.getSrc2() instanceof TableEntry) {
            int imm = ((Immediate) midCode.getSrc1()).getValue();
            int rt = allocReg((TableEntry) midCode.getSrc2(), true);
            int rd = allocReg(midCode.getDst(), false);
            switch (midCode.getOp()) {
                case ADD:
                    mipsObject.addAfter(new Addiu(rd, rt, imm));
                    break;
                case SUB:
                    mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                    mipsObject.addAfter(new Subu(rd, RF.GPR.V1, rt));

                    break;
                case MULT:
                    if (optimizer) {
                        mipsObject.addAfter(MultDiv.betterMult(rd, rt, imm));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Mul(rd, rt, RF.GPR.V1));
                    }
                    break;
                case DIV:
                    mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                    mipsObject.addAfter(new Div(RF.GPR.V1, rt));
                    mipsObject.addAfter(new Mflo(rd));
                    break;
                case MOD:
                    mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                    mipsObject.addAfter(new Div(RF.GPR.V1, rt));
                    mipsObject.addAfter(new Mfhi(rd));
                    break;
                case GRE:
                    mipsObject.addAfter(new Slti(rd, rt, imm));
                    break;
                case GEQ:
                    if (optimizer && betterBranch) {
                        mipsObject.addAfter(new Addiu(rd, rt, -imm - 1));
                        mipsObject.addAfter(new Slt(rd, rd, RF.GPR.ZERO));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Sge(rd, RF.GPR.V1, rt));
                    }
                    break;
                case EQL:
                    if (optimizer) {
                        mipsObject.addAfter(new Addiu(rd, rt, -1 * imm));
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rd));
                        mipsObject.addAfter(new Xori(rd, rd, 1));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Seq(rd, RF.GPR.V1, rt));
                    }
                    break;
                case NEQ:
                    if (optimizer) {
                        mipsObject.addAfter(new Addiu(rd, rt, -1 * imm));
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rd));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Sne(rd, RF.GPR.V1, rt));
                    }
                    break;
                case LSS:
                    mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                    mipsObject.addAfter(new Slt(rd, RF.GPR.V1, rt));
                    break;
                case LEQ:
                    if (optimizer && betterBranch) {
                        mipsObject.addAfter(new Addiu(rd, rt, imm * -1 + 1));
                        mipsObject.addAfter(new Slt(rd, RF.GPR.ZERO, rd));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Sle(rd, RF.GPR.V1, rt));
                    }
                    break;
            }
        } else if (midCode.getSrc1() instanceof TableEntry
                && midCode.getSrc2() instanceof Immediate) {
            int imm = ((Immediate) midCode.getSrc2()).getValue();
            int rs = allocReg((TableEntry) midCode.getSrc1(), true);
            int rd = allocReg(midCode.getDst(), false);
            switch (midCode.getOp()) {
                case ADD:
                    mipsObject.addAfter(new Addiu(rd, rs, imm));
                    break;
                case SUB:
                    mipsObject.addAfter(new Addiu(rd, rs, imm * -1));
                    break;
                case MULT:
                    if (optimizer) {
                        mipsObject.addAfter(MultDiv.betterMult(rd, rs, imm));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Mul(rd, rs, RF.GPR.V1));
                    }
                    break;
                case DIV:
                    if (optimizer) {
                        mipsObject.addAfter(MultDiv.betterDiv(rd, rs, imm));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Div(rs, RF.GPR.V1));
                        mipsObject.addAfter(new Mflo(rd));
                    }
                    break;
                case MOD:
                    if (optimizer) {
                        mipsObject.addAfter(MultDiv.betterMod(rd, rs, imm));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Div(rs, RF.GPR.V1));
                        mipsObject.addAfter(new Mfhi(rd));
                    }
                    break;
                case GEQ:
                    if (optimizer && betterBranch) {
                        mipsObject.addAfter(new Addiu(rd, rs, -imm + 1));
                        mipsObject.addAfter(new Slt(rd, RF.GPR.ZERO, rd));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Sge(rd, rs, RF.GPR.V1));
                    }
                    break;
                case GRE:
                    mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                    mipsObject.addAfter(new Sgt(rd, rs, RF.GPR.V1));
                    break;
                case EQL:
                    if (optimizer) {
                        mipsObject.addAfter(new Addiu(rd, rs, -1 * imm));
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rd));
                        mipsObject.addAfter(new Xori(rd, rd, 1));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Seq(rd, rs, RF.GPR.V1));
                    }
                    break;
                case NEQ:
                    if (optimizer) {
                        mipsObject.addAfter(new Addiu(rd, rs, -1 * imm));
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rd));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Sne(rd, rs, RF.GPR.V1));
                    }
                    break;
                case LEQ:
                    if (optimizer && betterBranch) {
                        mipsObject.addAfter(new Addiu(rd, rs, -imm - 1));
                        mipsObject.addAfter(new Slt(rd, rd, RF.GPR.ZERO));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Sle(rd, rs, RF.GPR.V1));
                    }
                    break;
                case LSS:
                    if (imm > (1 << 15) - 1) {
                        mipsObject.addAfter(new Li(RF.GPR.V1, imm));
                        mipsObject.addAfter(new Slt(rd, rs, RF.GPR.V1));
                    } else {
                        mipsObject.addAfter(new Slti(rd, rs, imm));
                    }
                    break;

            }
        }

    }

    public void unaryOperatorToMips(UnaryOperator midCode) {
        mipsObject.setComment(midCode.toIr());
        int rd = allocReg(midCode.getDst(), false);
        if (midCode.getSrc() instanceof TableEntry) {
            int rs = allocReg((TableEntry) midCode.getSrc(), true);
            switch (midCode.getOp()) {
                case PLUS:
                    mipsObject.addAfter(new Addu(rd, rs, RF.GPR.ZERO));
                    break;
                case MINU:
                    mipsObject.addAfter(new Subu(rd, RF.GPR.ZERO, rs));
                    break;
                case NOT:
                    if (optimizer) {
                        mipsObject.addAfter(new Sltu(rd, RF.GPR.ZERO, rs));
                        mipsObject.addAfter(new Xori(rd, rd, 1));
                    } else {
                        mipsObject.addAfter(new Seq(rd, rs, RF.GPR.ZERO));
                    }
                    break;
            }
        } else {
            int imm = ((Immediate) midCode.getSrc()).getValue();
            switch (midCode.getOp()) {
                case PLUS:
                    mipsObject.addAfter(new Li(rd, imm));
                    break;
                case MINU:
                    mipsObject.addAfter(new Li(rd, imm * -1));
                    break;
                case NOT:
                    mipsObject.addAfter(new Li(rd, imm == 0 ? 1 : 0));
                    break;
            }
        }
    }

    public int getBase(TableEntry tableEntry) {
        if (tableEntry.isGlobal) {
            return RF.GPR.GP;
        } else if (tableEntry.isTemp) {
            return RF.GPR.SP;
        } else {
            return RF.GPR.FP;
        }
    }

    public int getOffset(TableEntry tableEntry) {
        if (tableEntry.isGlobal) {
            return tableEntry.address;
        } else if (tableEntry.isTemp) {
            return tableEntry.address;
        } else {
            return -tableEntry.address;
        }
    }

    public void pointerOpToMips(PointerOp midCode) {
        mipsObject.setComment(midCode.toIr());
        if (midCode.getOp() == PointerOp.Op.STORE) {
            TableEntry dst = midCode.getDst();
            int base = getBase(dst);
            int offset = getOffset(dst);
            if (midCode.getSrc() instanceof Immediate) {
                if (dst.refType == TableEntry.RefType.ITEM) {
                    if (optimizer) {
                        int dstReg = allocReg(dst, false);
                        mipsObject.addAfter(new Li(dstReg, ((Immediate) midCode.getSrc()).getValue()));
                    } else {
                        mipsObject.addAfter(new Li(RF.GPR.V1, ((Immediate) midCode.getSrc()).getValue()));
                        mipsObject.addAfter(new Sw(RF.GPR.V1, offset, base));
                    }
                } else {
                    int dstReg = allocReg(dst, true);
                    mipsObject.addAfter(new Li(RF.GPR.V1, ((Immediate) midCode.getSrc()).getValue()));
                    mipsObject.addAfter(new Sw(RF.GPR.V1, 0, dstReg));
                }
            } else {
                int rs = allocReg((TableEntry) midCode.getSrc(), true);
                if (dst.refType == TableEntry.RefType.ITEM) {
                    //TODO: 图着色之后已分配的变量可以用move
                    if (optimizer) {
                        int dstReg = allocReg(dst, false);
                        mipsObject.addAfter(new Move(dstReg, rs));
                    } else {
                        mipsObject.addAfter(new Sw(rs, offset, base));
                    }

                } else {
                    int target = allocReg(dst, true);
                    mipsObject.addAfter(new Sw(rs, 0, target));
                }
            }

        } else if (midCode.getOp() == PointerOp.Op.LOAD) {
            int dst = allocReg(midCode.getDst(), false);
            if (midCode.getSrc() instanceof Immediate) {
                mipsObject.addAfter(new Li(dst, ((Immediate) midCode.getSrc()).getValue()));
            } else if (midCode.getSrc() instanceof TableEntry) {
                if (((TableEntry) midCode.getSrc()).refType == TableEntry.RefType.ITEM) {
                    TableEntry src = (TableEntry) midCode.getSrc();
                    //TODO: 图着色之后已分配的变量可以用move
                    if (optimizer) {
                        int srcReg = allocReg(src, true);
                        mipsObject.addAfter(new Move(dst, srcReg));
                    } else {
                        int base = getBase(src);
                        int offset = getOffset(src);
                        mipsObject.addAfter(new Lw(dst, offset, base));
                    }

                } else {
                    TableEntry src = (TableEntry) midCode.getSrc();
                    int reg = allocReg(src, true);
                    mipsObject.addAfter(new Lw(dst, 0, reg));
                }
            }
        }
    }

    public void callToMips(Call midCode) {

        mipsObject.addIrDescription("save fp, ra"); //保存fp,ra
        mipsObject.addAfter(new Sw(RF.GPR.FP, -4, RF.GPR.SP));
        mipsObject.addAfter(new Sw(RF.GPR.RA, -8, RF.GPR.SP));
        int offset = -12;
        if (optimizer) {
            mipsObject.addIrDescription("save regs"); //保存寄存器中的变量

            for (Integer reg : ColorResult.getAvailableReg()) {
                if (currentFuncDef.getColorResult().getAllocMap().get(reg).size() > 0) {
                    mipsObject.addAfter(new Sw(reg, offset, RF.GPR.SP));
                    offset -= 4;
                }
            }
        }
        mipsObject.setComment(midCode.toIr());
        mipsObject.addIrDescription("passing args");//传参数
        int argReg = 4;
        int addr = offset;
        for (Operand operand : midCode.getArgs()) {
            if (argReg < 8) { //前四个参数在寄存器
                if (operand instanceof Immediate) {
                    mipsObject.addAfter(new Li(argReg, ((Immediate) operand).getValue()));
                } else {
                    int src = allocReg((TableEntry) operand, true);
                    mipsObject.addAfter(new Move(argReg, src));
                }
                argReg += 1;
            } else {
                if (operand instanceof Immediate) {
                    mipsObject.addAfter(new Li(RF.GPR.V1, ((Immediate) operand).getValue()));
                    mipsObject.addAfter(new Sw(RF.GPR.V1, addr, RF.GPR.SP));
                } else {
                    int src = allocReg((TableEntry) operand, true);
                    mipsObject.addAfter(new Sw(src, addr, RF.GPR.SP));
                }
            }
            addr -= 4;
        }
        RegMap.saveTemp(midCode, mipsObject, currentBasicBlock);
        mipsObject.addIrDescription("set fp, sp for callee");//为被调用函数设置fp, sp
        mipsObject.addAfter(new Addiu(RF.GPR.FP, RF.GPR.SP, offset));
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, offset));
        //调用函数
        mipsObject.addAfter(new Jal(midCode.getFuncEntry().name()));
        mipsObject.addIrDescription("recover fp, ra for callee");//恢复ra和fp
        mipsObject.addAfter(new Lw(RF.GPR.RA, -offset - 8, RF.GPR.SP));
        mipsObject.addAfter(new Lw(RF.GPR.FP, -offset - 4, RF.GPR.SP));
        //恢复寄存器
        int offset1 = 12;
        if (optimizer) {
            for (Integer reg : ColorResult.getAvailableReg()) {
                if (currentFuncDef.getColorResult().getAllocMap().get(reg).size() > 0) {
                    mipsObject.addAfter(new Lw(reg, -offset - offset1, RF.GPR.SP));
                    offset1 += 4;
                }
            }
        }
        mipsObject.addIrDescription("recover sp");//恢复sp
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, offset1));

        if (midCode.getReturnDst() != null) {
            mipsObject.addIrDescription("get return value");//取返回值
            int dst = allocReg(midCode.getReturnDst(), false);
            mipsObject.addAfter(new Move(dst, RF.GPR.V0));
        }
    }

    public void returnToMips(Return midCode) {
        mipsObject.setComment(midCode.toIr());
        // set return value
        if (midCode.getReturnValue() != null) {
            if (midCode.getReturnValue() instanceof Immediate) {
                mipsObject.addAfter(new Li(RF.GPR.V0,
                        ((Immediate) midCode.getReturnValue()).getValue()));
            } else {
                int src = allocReg((TableEntry) midCode.getReturnValue(), true);
                mipsObject.addAfter(new Move(RF.GPR.V0, src));
            }
        }
        // 回收栈上临时变量
        getBackTempSpace();

        mipsObject.addAfter(new J(currentFunc.name() + "_EXIT"));
    }

    public void inputToMips(Input midCode) {
        mipsObject.setComment(midCode.toIr());
        int dst = allocReg(midCode.getDst(), false);
        mipsObject.addAfter(new Li(SYSCALL_REG, READ_INT_SYSCALL));
        mipsObject.addAfter(new Syscall());
        mipsObject.addAfter(new Move(dst, READ_REG));
    }

    public void printIntToMips(PrintInt midCode) {
        mipsObject.setComment(midCode.toIr());
        if (midCode.getValue() instanceof Immediate) {
            mipsObject.addAfter(new Li(PRINT_REG, ((Immediate) midCode.getValue()).getValue()));
            mipsObject.addAfter(new Li(SYSCALL_REG, PRINT_INT_SYSCALL));
            mipsObject.addAfter(new Syscall());
        } else if (midCode.getValue() instanceof TableEntry) {
            int src = allocReg((TableEntry) midCode.getValue(), true);
            mipsObject.addAfter(new Move(PRINT_REG, src));
            mipsObject.addAfter(new Li(SYSCALL_REG, PRINT_INT_SYSCALL));
            mipsObject.addAfter(new Syscall());
        }
    }

    public void printStrToMips(PrintStr midCode) {
        mipsObject.addAfter(new La(PRINT_REG, midCode.getLabel()));
        mipsObject.addAfter(new Li(SYSCALL_REG, PRINT_STR_SYSCALL));
        mipsObject.addAfter(new Syscall());
    }
}
