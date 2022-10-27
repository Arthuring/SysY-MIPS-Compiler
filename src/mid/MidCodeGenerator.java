package mid;

import front.FuncEntry;
import front.SemanticChecker;
import front.SymbolTable;
import front.nodes.CompileUnitNode;
import mid.ircode.BasicBlock;

import java.util.Map;

public class MidCodeGenerator {
    private static SymbolTable currentTable = SymbolTable.globalTable();
    private static final Map<String, FuncEntry> FUNC_TABLE = SemanticChecker.getFuncTable();
    private static FuncEntry currentFuncDef = null;
    private static BasicBlock currentBasicBlock = null;
    private static int depth = 1;

    public static void compileUnitToIr(CompileUnitNode compileUnitNode) {


    }

}
