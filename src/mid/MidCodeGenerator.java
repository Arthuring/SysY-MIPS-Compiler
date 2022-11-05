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
import mid.ircode.*;

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
    private static String currentLoopCond = null;
    private static String currentLoopBody = null;
    private static String endTag = "_end";

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
        String label = null;
        if (blockNode.type() == BlockNode.BlockType.BRANCH && currentBranchLabel != null) {
            label = currentBranchLabel;
            currentBranchLabel = null;
        } else if (blockNode.type() == BlockNode.BlockType.LOOP && currentLoopLabel != null) {
            label = currentLoopLabel;
            currentLoopLabel = null;
        }
        if (label == null) {
            label = LabelCounter.getLabel();
        }
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

    public static void addNewBasicBlock(String label) {
        currentBasicBlock = new BasicBlock(label);
        currentFuncDef.addBlock(currentBasicBlock);
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
            breakStmtNodeToIr((BreakStmtNode) stmtNode);
        } else if (stmtNode instanceof ContinueStmtNode) {
            continueStmtNodeToIr((ContinueStmtNode) stmtNode);
        } else if (stmtNode instanceof BlockNode) {
            blockNodeToIr((BlockNode) stmtNode);
        } else if (stmtNode instanceof WhileNode) {
            whileNodeToIr((WhileNode) stmtNode);
        } else if (stmtNode instanceof IfNode) {
            ifNodeToIr((IfNode) stmtNode);
        } else if (stmtNode instanceof ReturnNode) {
            returnNodeToIr((ReturnNode) stmtNode);
        }
    }

    public static void breakStmtNodeToIr(BreakStmtNode breakStmtNode) {
        currentBasicBlock.addAfter(new Jump(currentLoopBody + endTag));
        addNewBasicBlock(LabelCounter.getLabel());
    }

    public static void continueStmtNodeToIr(ContinueStmtNode continueStmtNode) {
        currentBasicBlock.addAfter(new Jump(currentLoopCond));
        addNewBasicBlock(LabelCounter.getLabel());
    }

    public static void whileNodeToIr(WhileNode whileNode) {
        String whileCondLabel = LabelCounter.getLabel("while_cond");
        String whileBodyLabel = LabelCounter.getLabel("while_body");
        String tempLoopCond = currentLoopCond;
        String tempLoopBody = currentLoopBody;
        currentLoopCond = whileCondLabel;
        currentLoopBody = whileBodyLabel;
        //branch
        addNewBasicBlock(whileCondLabel);
        ExprNode simplifiedCond = whileNode.cond().simplify(currentTable);
        Operand dst = expNodeToIr(simplifiedCond);
        currentBasicBlock.addAfter(new Branch(dst, whileBodyLabel,
                whileBodyLabel + endTag, Branch.BrOp.BEQ));
        //whileStmt
        currentLoopLabel = whileBodyLabel;
        blockNodeToIr((BlockNode) whileNode.whileStmt());
        //whileStmtEnd
        currentBasicBlock.addAfter(new Jump(whileCondLabel));
        currentBasicBlock.setEndLabel(whileBodyLabel + endTag);

        currentLoopCond = tempLoopCond;
        currentLoopBody = tempLoopBody;
        addNewBasicBlock(LabelCounter.getLabel());
    }


    public static void ifNodeToIr(IfNode ifNode) {
        ExprNode simplifiedCond = ifNode.cond().simplify(currentTable);
        Operand dst = expNodeToIr(simplifiedCond);
        if (ifNode.elseStmt() != null) {
            String ifLabel = LabelCounter.getLabel("if");
            String elseLabel = LabelCounter.getLabel("else");
            //branch
            currentBasicBlock.addAfter(new Branch(dst, ifLabel, elseLabel, Branch.BrOp.BNE));
            // elseStmt
            currentBranchLabel = elseLabel;
            blockNodeToIr((BlockNode) ifNode.elseStmt());
            currentBasicBlock.addAfter(new Jump(ifLabel + endTag));
            currentBasicBlock.setEndLabel(elseLabel + endTag);
            // ifStmt
            currentBranchLabel = ifLabel;
            blockNodeToIr((BlockNode) ifNode.ifStmt());
            currentBasicBlock.setEndLabel(ifLabel + endTag);
        } else {
            String ifLabel = LabelCounter.getLabel("if");
            //branch
            currentBasicBlock.addAfter(new Branch(dst, ifLabel, ifLabel + endTag, Branch.BrOp.BEQ));
            //ifStmt
            currentBranchLabel = ifLabel;
            blockNodeToIr((BlockNode) ifNode.ifStmt());
            currentBasicBlock.setEndLabel(ifLabel + endTag);
        }
        addNewBasicBlock(LabelCounter.getLabel());
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
    }
}
