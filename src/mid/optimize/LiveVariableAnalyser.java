package mid.optimize;

import mid.IrModule;
import mid.ircode.BasicBlock;
import mid.ircode.FuncDef;

public class LiveVariableAnalyser {
    public static IrModule reachingDefAnalysis(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            liveVariableAnalyseFunc(funcDef);
        }
        return irModule;
    }

    public static void liveVariableAnalyseFunc(FuncDef funcDef) {
        LiveVariableTable liveVariableTable = new LiveVariableTable();
        for (BasicBlock basicBlock : funcDef.getBasicBlocks()) {
            liveVariableTable.calculateDefUse(basicBlock);
        }
        liveVariableTable.calculateInOut(funcDef.getBasicBlocks());
        funcDef.setLiveVariableTable(liveVariableTable);
    }
}
