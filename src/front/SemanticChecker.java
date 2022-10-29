package front;

import exception.CompileExc;
import front.nodes.AssignNode;
import front.nodes.BinaryExpNode;
import front.nodes.BlockItemNode;
import front.nodes.BlockNode;
import front.nodes.BreakStmtNode;
import front.nodes.CompileUnitNode;
import front.nodes.ContinueStmtNode;
import front.nodes.DeclNode;
import front.nodes.DefNode;
import front.nodes.EmptyStmtNode;
import front.nodes.ExprNode;
import front.nodes.FuncCallNode;
import front.nodes.FuncDefNode;
import front.nodes.FuncParamNode;
import front.nodes.GetintNode;
import front.nodes.IfNode;
import front.nodes.LValNode;
import front.nodes.NumberNode;
import front.nodes.PrintfNode;
import front.nodes.ReturnNode;
import front.nodes.StmtNode;
import front.nodes.UnaryExpNode;
import front.nodes.WhileNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class SemanticChecker {
    private static SymbolTable currentTable = SymbolTable.globalTable();
    private static final Map<String, FuncEntry> FUNC_TABLE = new HashMap<>();
    private static final List<CompileExc> ERROR = new ArrayList<>();
    private static FuncEntry currentFunc = null;
    private static int depth = 0;
    private static BlockNode.BlockType currentBlock = BlockNode.BlockType.BASIC;
    private static int cycleDepth = 0;

    public static List<CompileExc> getError() {
        return ERROR;
    }

    public static CompileUnitNode buildCompileUnitNode(CompileUnit compileUnit) {
        assert compileUnit.type() == CompileUnit.Type.CompUnit;
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        FuncDefNode mainFuncDef;
        List<CompileUnit> childUnits = compileUnit.childUnits();
        for (CompileUnit childUnit : childUnits) {
            if (childUnit.type() == CompileUnit.Type.MainFuncDef) {
                break;
            } else if (childUnit.type() == CompileUnit.Type.FuncDef) {
                funcDefNodes.add(buildFuncDefNode(childUnit));
            } else {
                declNodes.add(buildDeclNode(childUnit));
            }
        }
        mainFuncDef = buildFuncDefNode(childUnits.get(childUnits.size() - 1));
        return new CompileUnitNode(declNodes, funcDefNodes, mainFuncDef);
    }

    public static DeclNode buildDeclNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.Decl);
        CompileUnit unit = compileUnit.childUnits().get(0);
        List<CompileUnit> childUnits = unit.childUnits();
        CompileUnit.Type type = unit.type() == CompileUnit.Type.ConstDecl ?
                childUnits.get(1).childUnits().get(0).type() :
                childUnits.get(0).childUnits().get(0).type();
        DeclNode declNode = new DeclNode(unit.type() == CompileUnit.Type.ConstDecl, type,
                childUnits.stream().filter(x -> x.type() == CompileUnit.Type.ConstDef ||
                                x.type() == CompileUnit.Type.VarDef)
                        .map(SemanticChecker::buildDefNode)
                        .collect(Collectors.toList()));
        List<DefNode> defNodeList = declNode.defNodeList();
        //add symbols to table
        for (DefNode defNode : defNodeList) {
            try {
                if (currentTable.isGlobalTable() && FUNC_TABLE.containsKey(defNode.ident())) {
                    throw new CompileExc(CompileExc.ErrType.REDEF, defNode.line());
                }
                currentTable.addVarSymbol(defNode, depth, declNode.isConst(), declNode.getType());
            } catch (CompileExc e) {
                ERROR.add(e);
            }

        }
        return declNode;
    }

    public static FuncDefNode buildFuncDefNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.FuncDef ||
                compileUnit.type() == CompileUnit.Type.MainFuncDef);
        List<CompileUnit> childUnits = compileUnit.childUnits();
        final CompileUnit.Type funcType = compileUnit.type() == CompileUnit.Type.FuncDef ?
                childUnits.get(0).childUnits().get(0).type() :
                childUnits.get(0).type();
        final String funcName = childUnits.get(1).name();
        final int line = childUnits.get(1).lineNo();
        List<FuncParamNode> funcParamNodes = new ArrayList<>();
        if (childUnits.get(3).type() == CompileUnit.Type.FuncFParams) {
            funcParamNodes.addAll(childUnits.get(3).childUnits().stream().
                    filter(x -> x.type() == CompileUnit.Type.FuncFParam)
                    .map(SemanticChecker::buildFuncParamNode).collect(Collectors.toList()));
        }
        //build a func entry
        FuncEntry funcEntry = new FuncEntry(funcName, funcType);
        //add arg to func entry
        for (FuncParamNode funcParamNode : funcParamNodes) {
            try {
                funcEntry.addArg(funcParamNode);
            } catch (CompileExc e) {
                ERROR.add(e);
            }
        }
        //check if redefine with var
        if (currentTable.getSymbol(funcName) != null) {
            ERROR.add(new CompileExc(CompileExc.ErrType.REDEF, line));
        }
        //check if redefine with func
        if (FUNC_TABLE.containsKey(funcName)) {
            ERROR.add(new CompileExc(CompileExc.ErrType.REDEF, line));
        } else {
            FUNC_TABLE.put(funcName, funcEntry);
        }
        FuncEntry tempFunc = currentFunc;
        currentFunc = funcEntry;
        BlockNode.BlockType temp = currentBlock;
        currentBlock = BlockNode.BlockType.FUNC;
        BlockNode funcBlock = buildBlockNode(childUnits.get(childUnits.size() - 1));

        currentBlock = temp;
        currentFunc = tempFunc;
        return new FuncDefNode(funcType, funcName, line, funcParamNodes, funcBlock);
    }

    public static DefNode buildDefNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.ConstDef ||
                compileUnit.type() == CompileUnit.Type.VarDef);
        CompileUnit identUnit = compileUnit.childUnits().get(0);
        List<CompileUnit> childUnit = compileUnit.childUnits();
        List<ExprNode> initValueList = new ArrayList<>();
        List<ExprNode> dim = new ArrayList<>();
        for (int i = 1; i < childUnit.size(); i++) {
            while (i < childUnit.size() && childUnit.get(i).type() == CompileUnit.Type.LBRACK) {
                dim.add(buildExprNode(childUnit.get(i + 1)));
                i += 3;
            }
            if (i < childUnit.size() && childUnit.get(i).type() == CompileUnit.Type.ASSIGN) {
                Queue<CompileUnit> processQueue = childUnit.get(i + 1).childUnits().stream().
                        filter(x -> x.type() == CompileUnit.Type.InitVal ||
                                x.type() == CompileUnit.Type.ConstExp ||
                                x.type() == CompileUnit.Type.ConstInitVal ||
                                x.type() == CompileUnit.Type.Exp)
                        .collect(Collectors.toCollection(LinkedList::new));
                while (!processQueue.isEmpty()) {
                    CompileUnit u = processQueue.poll();
                    if (u.type() == CompileUnit.Type.Exp || u.type() == CompileUnit.Type.ConstExp) {
                        initValueList.add(buildExprNode(u));
                    } else {
                        processQueue.addAll(u.childUnits().stream().
                                filter(x -> x.type() == CompileUnit.Type.InitVal ||
                                        x.type() == CompileUnit.Type.ConstExp ||
                                        x.type() == CompileUnit.Type.ConstInitVal ||
                                        x.type() == CompileUnit.Type.Exp
                                ).collect(Collectors.toList()));
                    }
                }
            }
        }
        return new DefNode(identUnit.name(), identUnit.lineNo(), dim, initValueList);
    }

    public static FuncParamNode buildFuncParamNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.FuncFParam);
        List<CompileUnit> childUnits = compileUnit.childUnits();
        CompileUnit.Type type = childUnits.get(0).childUnits().get(0).type();
        String paraName = childUnits.get(1).name();
        int line = childUnits.get(1).lineNo();
        List<ExprNode> dimension = new ArrayList<>();
        for (int i = 2; i < childUnits.size(); i++) {
            int dimLayer = 0;
            while (i < childUnits.size() && childUnits.get(i).type() == CompileUnit.Type.LBRACK) {
                if (dimLayer == 0) {
                    dimension.add(new NumberNode(0));
                    i += 2;
                    dimLayer += 1;
                } else {
                    dimension.add(buildExprNode(childUnits.get(i + 1)));
                    i += 3;
                }
            }
        }
        return new FuncParamNode(type, paraName, line, dimension);
    }

    public static BlockNode buildBlockNode(CompileUnit compileUnit) {
        //new layer for symbolTable and depth
        SymbolTable temp = currentTable;
        depth += 1;
        currentTable = new SymbolTable(temp, currentFunc.name() + depth);

        if (depth == 1) {
            for (TableEntry tableEntry : currentFunc.args()) {
                currentTable.addVarSymbol(tableEntry);
            }
        }
        List<CompileUnit> compileUnits = compileUnit.childUnits();
        BlockNode blockNode = new BlockNode(compileUnits.stream().
                filter(x -> x.type() == CompileUnit.Type.BlockItem)
                .map(SemanticChecker::buildBlockItemNode)
                .collect(Collectors.toList()), currentBlock);

        List<BlockItemNode> blockItemNodes = blockNode.blockItemNodes();
        if (currentBlock == BlockNode.BlockType.FUNC &&
                currentFunc.returnType() != TableEntry.ValueType.VOID) {

            if (depth == 1 &&
                    (blockItemNodes.size() == 0 ||
                            !(blockItemNodes.get(blockItemNodes.size() - 1)
                                    instanceof ReturnNode))) {
                ERROR.add(new CompileExc(CompileExc.ErrType.MISSING_RET,
                        compileUnits.get(compileUnits.size() - 1).lineNo()));
            }
        }
        //set symbolTable & FuncEntry in BlockNode
        blockNode.setFuncEntry(currentFunc);
        blockNode.setSymbolTable(currentTable);
        blockNode.setDepth(depth);

        // recover symbolTable and depth
        currentTable = temp;
        depth -= 1;
        return blockNode;
    }

    public static BlockItemNode buildBlockItemNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.BlockItem);
        List<CompileUnit> childUnit = compileUnit.childUnits();
        if (childUnit.get(0).type() == CompileUnit.Type.Decl) {
            return buildDeclNode(childUnit.get(0));
        } else {
            return buildStmtNode(childUnit.get(0));
        }
    }

    public static StmtNode buildStmtNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.Stmt);
        List<CompileUnit> childUnits = compileUnit.childUnits();
        if (childUnits.size() == 1 && childUnits.get(0).type() == CompileUnit.Type.Block) {
            return buildBlockNode(childUnits.get(0));
        } else {
            switch (childUnits.get(0).type()) {
                case IFTK:
                    return buildIfNode(compileUnit);
                case WHILETK:
                    return buildWhileNode(compileUnit);
                case CONTINUETK:
                    ContinueStmtNode continueStmtNode = buildContinueStmtNode(compileUnit);
                    if (cycleDepth == 0) {
                        ERROR.add(new CompileExc(CompileExc.ErrType.NOT_IN_LOOP, continueStmtNode.line()));
                    }
                    return continueStmtNode;
                case BREAKTK:
                    BreakStmtNode breakStmtNode = buildBreakStmtNode(compileUnit);
                    if (cycleDepth == 0) {
                        ERROR.add(new CompileExc(CompileExc.ErrType.NOT_IN_LOOP, breakStmtNode.line()));
                    }
                    return breakStmtNode;
                case RETURNTK:
                    return buildReturnNode(compileUnit);
                case PRINTFTK:
                    PrintfNode printfNode = new PrintfNode(
                            childUnits.get(0).lineNo(),
                            childUnits.get(2).name(),
                            childUnits.stream()
                                    .filter(x -> x.type() == CompileUnit.Type.Exp)
                                    .map(SemanticChecker::buildExprNode)
                                    .collect(Collectors.toList())
                    );
                    try {
                        printfNode.checkArgNum();
                    } catch (CompileExc e) {
                        ERROR.add(e);
                    }
                    try {
                        printfNode.checkFormatString();
                    } catch (CompileExc e) {
                        ERROR.add(e);
                    }
                    return printfNode;
                case SEMICN:
                    return new EmptyStmtNode();
                case LVal:
                    return buildAssignNode(compileUnit);
                default:
                    return buildExprNode(childUnits.get(0));
            }
        }

    }

    public static AssignNode buildAssignNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnit = compileUnit.childUnits();
        if (childUnit.get(2).type() == CompileUnit.Type.GETINTTK) {
            AssignNode assignNode = new AssignNode(buildLValNode(childUnit.get(0)),
                    new GetintNode(childUnit.get(2).lineNo()));
            TableEntry tableEntry = currentTable.getSymbol(assignNode.lVal().ident());
            if (tableEntry != null && tableEntry.isConst) {
                ERROR.add(new CompileExc(CompileExc.ErrType.CHANGE_CONST, assignNode.lVal().line()));
            }
            return assignNode;
        }
        AssignNode assignNode = new AssignNode(buildLValNode(childUnit.get(0)),
                buildExprNode(childUnit.get(2)));
        TableEntry tableEntry = currentTable.getSymbol(assignNode.lVal().ident());
        if (tableEntry != null && tableEntry.isConst) {
            ERROR.add(new CompileExc(CompileExc.ErrType.CHANGE_CONST, assignNode.lVal().line()));
        }
        return assignNode;
    }

    public static IfNode buildIfNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnits = compileUnit.childUnits();
        ExprNode cond = buildExprNode(childUnits.get(2));
        StmtNode ifStmt = buildStmtNode(childUnits.get(4));
        if (childUnits.size() >= 6 && childUnits.get(5).type() == CompileUnit.Type.ELSETK) {
            StmtNode elseStmt = buildStmtNode(childUnits.get(childUnits.size() - 1));
            return new IfNode(cond, ifStmt, elseStmt);
        }
        return new IfNode(cond, ifStmt);
    }

    public static WhileNode buildWhileNode(CompileUnit compileUnit) {
        cycleDepth += 1;
        List<CompileUnit> childUnits = compileUnit.childUnits();
        ExprNode cond = buildExprNode(childUnits.get(2));
        StmtNode whileStmt = buildStmtNode(childUnits.get(childUnits.size() - 1));
        cycleDepth -= 1;
        return new WhileNode(cond, whileStmt);
    }

    public static LValNode buildLValNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnits = compileUnit.childUnits();
        String ident = childUnits.get(0).name();
        int line = childUnits.get(0).lineNo();
        List<ExprNode> indexes = new ArrayList<>();
        for (int i = 1; i < childUnits.size(); i++) {
            while (i < childUnits.size() && childUnits.get(i).type() == CompileUnit.Type.LBRACK) {
                indexes.add(buildExprNode(childUnits.get(i + 1)));
                i += 3;
            }
        }

        TableEntry tableEntry = currentTable.getSymbol(ident);
        TableEntry.ValueType valueType = TableEntry.ValueType.INT;
        int dimension = indexes.size();
        if (tableEntry == null) {
            ERROR.add(new CompileExc(CompileExc.ErrType.UNDECL, line));
        } else {
            dimension = tableEntry.dimension.size();
            valueType = tableEntry.valueType;
        }
        return new LValNode(ident, line, indexes, dimension, valueType);
    }

    public static ExprNode buildExprNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnits = compileUnit.childUnits();
        switch (compileUnit.type()) {
            case Exp:
            case Cond:
            case ConstExp:
                return buildExprNode(childUnits.get(0));
            case LOrExp:
            case LAndExp:
            case EqExp:
            case RelExp:
            case AddExp:
            case MulExp:
                if (childUnits.size() == 1) {
                    return buildExprNode(childUnits.get(0));
                } else {
                    return new BinaryExpNode(buildExprNode(childUnits.get(0)),
                            childUnits.get(1).type(), buildExprNode(childUnits.get(2)));
                }
            case UnaryExp:
                if (childUnits.size() == 1) {
                    return buildExprNode(childUnits.get(0));
                } else if (childUnits.get(0).type() == CompileUnit.Type.UnaryOp) {
                    return new UnaryExpNode(childUnits.get(0).childUnits().get(0).type(),
                            buildExprNode(childUnits.get(1)));
                } else {
                    return buildFuncCallNode(compileUnit);
                }
            case PrimaryExp:
                if (childUnits.size() == 1) {
                    return buildExprNode(childUnits.get(0));
                } else {
                    return buildExprNode(childUnits.get(1));
                }
            case LVal:
                return buildLValNode(compileUnit);
            case Number:
                return buildNumberNode(compileUnit);
            default:
                return null;
        }
    }

    public static NumberNode buildNumberNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.Number);
        return new NumberNode(Integer.parseInt(compileUnit.childUnits().get(0).name()));
    }

    public static BreakStmtNode buildBreakStmtNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnit = compileUnit.childUnits();
        int line = childUnit.get(0).lineNo();
        return new BreakStmtNode(line);
    }

    public static ContinueStmtNode buildContinueStmtNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnit = compileUnit.childUnits();
        int line = childUnit.get(0).lineNo();
        return new ContinueStmtNode(line);
    }

    public static ReturnNode buildReturnNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnit = compileUnit.childUnits();
        int line = childUnit.get(0).lineNo();
        ReturnNode returnNode = new ReturnNode(line, buildExprNode(childUnit.get(1)));
        if (currentBlock == BlockNode.BlockType.FUNC &&
                currentFunc.returnType() == TableEntry.ValueType.VOID) {
            if (returnNode.returnExpr() != null) {
                if (returnNode.returnExpr().valueType != TableEntry.ValueType.VOID) {
                    ERROR.add(new CompileExc(CompileExc.ErrType.RET_TYPE_MISMATCH, returnNode.line()));
                }
            }
        }
        return returnNode;
    }

    public static FuncCallNode buildFuncCallNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnit = compileUnit.childUnits();
        String ident = childUnit.get(0).name();
        int line = childUnit.get(0).lineNo();
        List<ExprNode> args = new ArrayList<>();
        if (childUnit.size() > 3 && childUnit.get(2).type() == CompileUnit.Type.FuncRParams) {
            args.addAll(childUnit.get(2).childUnits().stream().
                    filter(x -> x.type() == CompileUnit.Type.Exp).
                    map(SemanticChecker::buildExprNode).collect(Collectors.toList()));
        }
        //find table for callee func
        TableEntry tableEntry = currentTable.getSymbol(ident);
        TableEntry.ValueType valueType = TableEntry.ValueType.INT;
        if (tableEntry != null || !FUNC_TABLE.containsKey(ident)) {
            ERROR.add(new CompileExc(CompileExc.ErrType.UNDECL, line));
        } else {
            FuncEntry funcEntry = FUNC_TABLE.get(ident);
            valueType = funcEntry.returnType();
            if (funcEntry.args().size() != args.size()) {
                ERROR.add(new CompileExc(CompileExc.ErrType.ARG_NUM_MISMATCH, line));
            } else {
                for (int i = 0; i < args.size(); i++) {
                    TableEntry funcArg = funcEntry.args().get(i);
                    ExprNode arg = args.get(i);
                    if (funcArg.valueType != arg.valueType) {
                        ERROR.add(new CompileExc(CompileExc.ErrType.ARG_TYPE_MISMATCH, line));
                    } else if (funcArg.dimension.size() != arg.dimension) {
                        ERROR.add(new CompileExc(CompileExc.ErrType.ARG_TYPE_MISMATCH, line));
                    }
                }
            }
        }
        return new FuncCallNode(ident, line, args, valueType);
    }

    public static SymbolTable getCurrentTable() {
        return currentTable;
    }

    public static Map<String, FuncEntry> getFuncTable() {
        return FUNC_TABLE;
    }
}
