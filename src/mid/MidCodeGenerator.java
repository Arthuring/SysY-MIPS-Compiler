package mid;

import front.FuncEntry;
import front.SemanticChecker;
import front.SymbolTable;
import front.TableEntry;
import front.nodes.*;
import javafx.scene.control.Tab;
import mid.ircode.*;

import java.util.List;
import java.util.Map;

public class MidCodeGenerator {
    private static SymbolTable currentTable = SymbolTable.globalTable();
    private static final Map<String, FuncEntry> FUNC_TABLE = SemanticChecker.getFuncTable();
    private static FuncDef currentFuncDef = null;
    private static BasicBlock currentBasicBlock = null;
    private static int depth = 1;

    public static void compileUnitToIr(CompileUnitNode compileUnitNode) {
        List<DeclNode> declNodes = compileUnitNode.declNodes();
        List<FuncDefNode> funcDefNodes = compileUnitNode.funcDefNodes();
        FuncDefNode mainFuncDef = compileUnitNode.mainFuncDef();
        for (DeclNode declNode : declNodes) {
            declNodeToIr(declNode);
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            funcDefNodeToIr(funcDefNode);
        }
    }

    public static void declNodeToIr(DeclNode declNode) {
        List<DefNode> defNodeList = declNode.defNodeList();
        for (DefNode defNode : defNodeList) {
            TableEntry tableEntry = currentTable.getSymbol(defNode.ident());
            tableEntry.simplify(currentTable);
            if (currentTable.isGlobalTable()) {
                IrModule.GLOBAL_VAR_DEFS.add(tableEntry);
            } else {
                VarDef varDef = new VarDef(tableEntry);
                currentBasicBlock.addAfter(varDef);
            }
        }
    }

    public static void funcDefNodeToIr(FuncDefNode funcDefNode) {
        FuncDef temp = currentFuncDef;
        currentFuncDef = new FuncDef(FUNC_TABLE.get(funcDefNode.name()));

        blockNodeToIr(funcDefNode.blockNode());

        currentFuncDef = temp;
    }

    public static void blockNodeToIr(BlockNode blockNode) {
        List<BlockItemNode> blockItemNodes = blockNode.blockItemNodes();
        //切换环境
        SymbolTable temp = currentTable;
        currentTable = blockNode.getSymbolTable();
        depth += 1;
        //new basic block
        String label = currentBasicBlock.getLabel();
        currentBasicBlock = new BasicBlock(label);
        currentFuncDef.addBlock(currentBasicBlock);

        for (BlockItemNode blockItemNode : blockItemNodes) {
            if (blockItemNode instanceof DeclNode) {
                declNodeToIr((DeclNode) blockItemNode);
            } else {
                stmtNodeToIr((StmtNode) blockItemNode);
            }
        }

        currentTable = temp;
        depth -= 1;
    }

    public static void stmtNodeToIr(StmtNode stmtNode) {
        if (stmtNode instanceof AssignNode) {
            assignNodeToIr((AssignNode) stmtNode);
        } else if (stmtNode instanceof GetintNode) {
            getIntNodeToIr((GetintNode) stmtNode);
        } else if (stmtNode instanceof PrintfNode) {
            printfNodeToIr((PrintfNode) stmtNode);
        } else if (stmtNode instanceof ExprNode) {
            ExprNode exprNode = ((ExprNode)stmtNode).simplify(currentTable);
            expNodeToIr(exprNode);
        } else if (stmtNode instanceof BreakStmtNode) {
            //TODO
        } else if (stmtNode instanceof ContinueStmtNode) {
            //TODO
        } else if (stmtNode instanceof BlockNode) {
            blockNodeToIr((BlockNode) stmtNode);
        } else if (stmtNode instanceof WhileNode) {
            //TODO
        } else if (stmtNode instanceof IfNode) {
            //TODO
        } else if (stmtNode instanceof ReturnNode) {
            returnNodeToIr((ReturnNode) stmtNode);
        }
    }

    public static void assignNodeToIr(AssignNode assignNode) {
        LValNode left = assignNode.lVal();
        TableEntry dst = currentTable.getSymbol(left.ident());
        ExprNode right = assignNode.exprNode().simplify(currentTable);
        Operand value = expNodeToIr(right);
        currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, dst, value));
    }

    public static Operand expNodeToIr(ExprNode exprNode) {
        if (exprNode instanceof BinaryExpNode) {
            return binaryExpNodeToIr((BinaryExpNode) exprNode);
        } else {
            return unaryExpNodeToIr((UnaryExpNode) exprNode);
        }
    }

    public static Operand binaryExpNodeToIr(BinaryExpNode exprNode) {
        Operand left = expNodeToIr(exprNode.left());
        Operand right = expNodeToIr(exprNode.right());
        TableEntry dst = TempCounter.getTemp();
        currentBasicBlock.addAfter(new BinaryOperator(exprNode.op(),dst, left, right));
        return dst;
    }

    public static Operand unaryExpNodeToIr(UnaryExpNode exprNode) {
        return null;
    }

    public static void getIntNodeToIr(GetintNode getintNode) {

    }

    public static Operand funcCallNodeToIr(FuncCallNode funcCallNode) {
        return null;
    }

    public static Operand LValNodeToIr(LValNode lValNode) {
        return null;
    }

    public static void returnNodeToIr(ReturnNode returnNode) {

    }

    public static void printfNodeToIr(PrintfNode printfNode) {

    }
}
