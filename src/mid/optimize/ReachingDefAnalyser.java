package mid.optimize;

import mid.IrModule;
import mid.ircode.BasicBlock;
import mid.ircode.FuncDef;
import mid.ircode.InstructionLinkNode;

public class ReachingDefAnalyser {
    public static IrModule reachingDefAnalysis(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            reachingDefAnalyseFunc(funcDef);
        }
        return irModule;
    }

    public static void reachingDefAnalyseFunc(FuncDef funcDef) {
        ReachingDefTable reachingDefTable = new ReachingDefTable();
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            signDef(basicBlock, reachingDefTable);
        }
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            reachingDefTable.calculateGenKill(basicBlock);
        }
        reachingDefTable.calculateInOut(funcDef.getBasicBlocks());
        funcDef.setReachingDefTable(reachingDefTable);
    }

    public static void signDef(BasicBlock basicBlock, ReachingDefTable reachingDefTable) {
        InstructionLinkNode ptr = basicBlock.next();
        while (ptr != basicBlock.getEnd()) {
            reachingDefTable.signDef(ptr);
            ptr = ptr.next();
        }
    }

}
