package mid;

import front.FuncEntry;
import front.SemanticChecker;
import front.SymbolTable;
import front.TableEntry;
import front.nodes.AssignNode;
import front.nodes.BinaryExpNode;
import front.nodes.BlockItemNode;
import front.nodes.BlockNode;
import front.nodes.BreakStmtNode;
import front.nodes.CompileUnitNode;
import front.nodes.ContinueStmtNode;
import front.nodes.DeclNode;
import front.nodes.DefNode;
import front.nodes.ExprNode;
import front.nodes.FuncCallNode;
import front.nodes.FuncDefNode;
import front.nodes.GetintNode;
import front.nodes.IfNode;
import front.nodes.LValNode;
import front.nodes.NumberNode;
import front.nodes.PrintfNode;
import front.nodes.ReturnNode;
import front.nodes.StmtNode;
import front.nodes.UnaryExpNode;
import front.nodes.WhileNode;
import mid.ircode.BasicBlock;
import mid.ircode.BinaryOperator;
import mid.ircode.Call;
import mid.ircode.FuncDef;
import mid.ircode.Immediate;
import mid.ircode.Input;
import mid.ircode.Operand;
import mid.ircode.PointerOp;
import mid.ircode.PrintInt;
import mid.ircode.PrintStr;
import mid.ircode.Return;
import mid.ircode.UnaryOperator;
import mid.ircode.VarDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MidCodeGenerator {
    private static SymbolTable currentTable = SymbolTable.globalTable();
    private static final Map<String, FuncEntry> FUNC_TABLE = SemanticChecker.getFuncTable();
    private static FuncDef currentFuncDef = null;
    private static BasicBlock currentBasicBlock = null;
    private static int depth = 1;
    private static final IrModule IR_MODULE = new IrModule();
    private static String currentBranchLabel = null;
    private static String currentLoopLabel = null;

    public static IrModule compileUnitToIr(CompileUnitNode compileUnitNode) {
        List<DeclNode> declNodes = compileUnitNode.declNodes();
        List<FuncDefNode> funcDefNodes = compileUnitNode.funcDefNodes();
        FuncDefNode mainFuncDef = compileUnitNode.mainFuncDef();
        for (DeclNode declNode : declNodes) {
            declNodeToIr(declNode);
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            funcDefNodeToIr(funcDefNode);
        }
        funcDefNodeToIr(mainFuncDef);
        return IR_MODULE;
    }

    public static void declNodeToIr(DeclNode declNode) {
        List<DefNode> defNodeList = declNode.defNodeList();
        if (!declNode.isConst()) {
            for (DefNode defNode : defNodeList) {
                TableEntry tableEntry = currentTable.getSymbol(defNode.ident());
                tableEntry.simplify(currentTable);
                if (currentTable.isGlobalTable()) {
                    IR_MODULE.getGlobalVarDefs().add(tableEntry);
                } else {
                    VarDef varDef = new VarDef(tableEntry);
                    currentBasicBlock.addAfter(varDef);
                    if ((tableEntry.refType == TableEntry.RefType.ITEM &&
                            tableEntry.initValue != null)) {
                        Operand init = expNodeToIr(tableEntry.initValue);
                        currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, tableEntry, init));
                    }
                }
            }
        } else {
            for (DefNode defNode : defNodeList) {
                TableEntry tableEntry = currentTable.getSymbol(defNode.ident());
                tableEntry.simplify(currentTable);
            }
        }
    }

    public static void funcDefNodeToIr(FuncDefNode funcDefNode) {
        FuncDef temp = currentFuncDef;

        currentFuncDef = new FuncDef(FUNC_TABLE.get(funcDefNode.name()));
        blockNodeToIr(funcDefNode.blockNode());
        IR_MODULE.getFuncDefs().add(currentFuncDef);

        currentFuncDef = temp;
    }

    public static void blockNodeToIr(BlockNode blockNode) {
        List<BlockItemNode> blockItemNodes = blockNode.blockItemNodes();
        //切换环境
        SymbolTable temp = currentTable;
        currentTable = blockNode.getSymbolTable();
        currentFuncDef.addLocalVar(currentTable);
        depth += 1;
        //new basic block
        String label = LabelCounter.getLabel();
        currentBasicBlock = new BasicBlock(label);
        currentFuncDef.addBlock(currentBasicBlock);

        for (BlockItemNode blockItemNode : blockItemNodes) {
            if (blockItemNode instanceof DeclNode) {
                declNodeToIr((DeclNode) blockItemNode);
            } else {
                stmtNodeToIr((StmtNode) blockItemNode);
            }
        }

        //切换环境
        currentTable = temp;
        depth -= 1;
    }

    public static void stmtNodeToIr(StmtNode stmtNode) {
        if (stmtNode instanceof AssignNode) {
            assignNodeToIr((AssignNode) stmtNode);
        } else if (stmtNode instanceof PrintfNode) {
            printfNodeToIr((PrintfNode) stmtNode);
        } else if (stmtNode instanceof ExprNode) {
            ExprNode exprNode = ((ExprNode) stmtNode).simplify(currentTable);
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

    public static void ifNodeToIr(IfNode ifNode){
        ExprNode simplifiedCond = ifNode.cond().simplify(currentTable);
        Operand dst = expNodeToIr(simplifiedCond);
        if(ifNode.elseStmt() != null){
            currentBranchLabel = LabelCounter.getLabel();
            blockNodeToIr(ifNode.elseStmt());
        }

    }

    public static void assignNodeToIr(AssignNode assignNode) {
        LValNode left = assignNode.lVal();
        TableEntry dst = currentTable.getSymbol(left.ident());
        ExprNode right = assignNode.exprNode().simplify(currentTable);
        if (right instanceof GetintNode) {
            Operand value = getIntNodeToIr((GetintNode) right);
            currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, dst, value));
        } else {
            Operand value = expNodeToIr(right);
            currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.STORE, dst, value));
        }
    }

    public static Operand expNodeToIr(ExprNode exprNode) {
        if (exprNode instanceof BinaryExpNode) {
            return binaryExpNodeToIr((BinaryExpNode) exprNode);
        } else if (exprNode instanceof UnaryExpNode) {
            return unaryExpNodeToIr((UnaryExpNode) exprNode);
        } else if (exprNode instanceof GetintNode) {
            return getIntNodeToIr((GetintNode) exprNode);
        } else if (exprNode instanceof FuncCallNode) {
            return funcCallNodeToIr((FuncCallNode) exprNode);
        } else if (exprNode instanceof LValNode) {
            return LValNodeToIr((LValNode) exprNode);
        } else if (exprNode instanceof NumberNode) {
            return new Immediate(((NumberNode) exprNode).number());
        } else {
            return null;
        }
    }

    public static Operand binaryExpNodeToIr(BinaryExpNode exprNode) {
        Operand left = expNodeToIr(exprNode.left());
        Operand right = expNodeToIr(exprNode.right());
        TableEntry dst = TempCounter.getTemp();
        currentBasicBlock.addAfter(new BinaryOperator(exprNode.op(), dst, left, right));
        return dst;
    }

    public static Operand unaryExpNodeToIr(UnaryExpNode exprNode) {
        Operand right = expNodeToIr(exprNode.expNode());
        if (exprNode.op() != UnaryExpNode.UnaryOp.PLUS) {
            TableEntry dst = TempCounter.getTemp();
            currentBasicBlock.addAfter(new UnaryOperator(exprNode.op(), dst, right));
            return dst;
        } else {
            return right;
        }
    }

    public static Operand getIntNodeToIr(GetintNode getintNode) {
        TableEntry dst = TempCounter.getTemp();
        currentBasicBlock.addAfter(new Input(dst));
        return dst;
    }

    public static Operand funcCallNodeToIr(FuncCallNode funcCallNode) {
        FuncEntry funcEntry = FUNC_TABLE.get(funcCallNode.ident());
        TableEntry dst = funcEntry.returnType() == TableEntry.ValueType.INT ?
                TempCounter.getTemp() : null;
        List<Operand> irArgs = new ArrayList<>();
        List<ExprNode> args = funcCallNode.args();
        for (ExprNode exprNode : args) {
            ExprNode simplifyedExpr = exprNode.simplify(currentTable);
            irArgs.add(expNodeToIr(simplifyedExpr));
        }
        if (funcEntry.returnType() == TableEntry.ValueType.VOID) {
            currentBasicBlock.addAfter(new Call(funcEntry, irArgs));
        } else {
            currentBasicBlock.addAfter(new Call(funcEntry, irArgs, dst));
        }
        return dst;
    }

    public static Operand LValNodeToIr(LValNode lValNode) {
        TableEntry dst = TempCounter.getTemp();
        TableEntry src = currentTable.getSymbol(lValNode.ident());
        currentBasicBlock.addAfter(new PointerOp(PointerOp.Op.LOAD, dst, src));
        return dst;
    }

    public static void returnNodeToIr(ReturnNode returnNode) {
        Operand returnValue;
        if (returnNode.returnExpr() != null) {
            ExprNode returnExpr = returnNode.returnExpr().simplify(currentTable);
            returnValue = expNodeToIr(returnExpr);
            currentBasicBlock.addAfter(new Return(returnValue));
        } else {
            currentBasicBlock.addAfter(new Return(null));
        }

    }

    public static void printfNodeToIr(PrintfNode printfNode) {
        String formatString = printfNode.formatString();
        List<String> formatStrings = Arrays.stream((formatString.split("(?<=%d)|(?=%d)")))
                .collect(Collectors.toList());
        int putIntCnt = 0;
        List<ExprNode> args = printfNode.args();
        for (String string : formatStrings) {
            if (!string.equals("%d")) {
                String label = StringCounter.findString(string);
                currentBasicBlock.addAfter(new PrintStr(label, string));
            } else {
                ExprNode simplifiedExpr = args.get(putIntCnt).simplify(currentTable);
                Operand result = expNodeToIr(simplifiedExpr);
                currentBasicBlock.addAfter(new PrintInt(result));
                putIntCnt += 1;
            }
        }
//        String formatString = printfNode.formatString();
//        int begin = 0;
//        int argCnt = 0;
//        List<ExprNode> args = printfNode.args();
//        for (int end = 0; end < formatString.length(); end++) {
//            if (formatString.charAt(end) == '%') {
//                String label = StringCounter.findString(formatString.substring(begin, end));
//                currentBasicBlock.addAfter(new PrintStr(label, formatString.substring(begin, end)));
//                end += 1;
//                Operand result = expNodeToIr(args.get(argCnt));
//                currentBasicBlock.addAfter(new PrintInt(result));
//                begin = end + 2;
//            }
//        }
//        if (begin < formatString.length()) {
//            String label = StringCounter.findString(formatString.substring(begin));
//            currentBasicBlock.addAfter(new PrintStr(label, formatString.substring(begin)));
//        }
    }
}
