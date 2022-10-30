package back;

import back.hardware.Memory;
import back.hardware.RF;
import back.instr.Addiu;
import back.instr.Move;
import back.instr.Sw;
import front.FuncEntry;
import front.TableEntry;
import front.nodes.UnaryExpNode;
import mid.IrModule;
import mid.ircode.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将LLVM ir 翻译为 MIPS
 */

public class Translator {
    private final IrModule irModule;
    private final MipsObject mipsObject = new MipsObject();
    private FuncEntry currentFunc = null;
    private int currentStackSize = 0;

    public Translator(IrModule irModule) {
        this.irModule = irModule;
    }

    public IrModule getIrModule() {
        return irModule;
    }

    public MipsObject toMips() {
        globalToMips();
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
        currentStackSize = funcDef.calculateStackSpace();

        //函数入口
        mipsObject.addAfter(new Sw(RF.GPR.RA, -4, RF.GPR.SP, funcDef.getFuncEntry().name()));
        mipsObject.addAfter(new Addiu(RF.GPR.FP, RF.GPR.SP, -4));
        mipsObject.addAfter(new Addiu(RF.GPR.SP, RF.GPR.SP, -1 * currentStackSize));

        //函数基本快
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            basicBlockToMips(basicBlock);
        }
        //TODO:函数出口
    }

    private int tempVarAddr = 0; //相对sp的偏移
    private final Map<TableEntry, Integer> tempRefCounter = new HashMap<>();

    /**
     * 对临时变量引用计数，并分配栈上地址
     */
    public void loadTempVar(BasicBlock basicBlock) {
        tempRefCounter.clear();
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
        
    }

}
