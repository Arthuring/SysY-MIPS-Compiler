package front;

import front.nodes.*;

import java.util.*;
import java.util.stream.Collectors;

public class SyntaxTreeBuilder {
    public static CompileUnitNode buildCompileUnitNode(CompileUnit compileUnit) {
        assert compileUnit.type() == CompileUnit.Type.CompUnit;
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        FuncDefNode mainFuncDef = null;
        for (CompileUnit childUnit : compileUnit.childUnits()) {
            if (childUnit.type() == CompileUnit.Type.MainFuncDef) {
                break;
            } else if (childUnit.type() == CompileUnit.Type.FuncDef) {
                funcDefNodes.add(buildFuncDefNode(childUnit));
            } else {
                declNodes.add(buildDeclNode(childUnit));
            }
        }
        mainFuncDef = buildFuncDefNode(compileUnit.childUnits().get(compileUnit.childUnits().size() - 1));
        return new CompileUnitNode(declNodes, funcDefNodes, mainFuncDef);
    }

    public static DeclNode buildDeclNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.Decl);
        CompileUnit unit = compileUnit.childUnits().get(0);
        List<CompileUnit> childUnits = unit.childUnits();
        return new DeclNode(childUnits.get(0).type() == CompileUnit.Type.ConstDecl,
                childUnits.stream().filter(x -> x.type() == CompileUnit.Type.ConstDef || x.type() ==
                        CompileUnit.Type.VarDef).map(SyntaxTreeBuilder::buildDefNode).collect(Collectors.toList()));
    }

    public static FuncDefNode buildFuncDefNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.FuncDef ||
                compileUnit.type() == CompileUnit.Type.MainFuncDef);
        List<CompileUnit> childUnits = compileUnit.childUnits();
        CompileUnit.Type funcType = childUnits.get(0).type();
        String funcName = childUnits.get(1).name();
        int line = childUnits.get(1).lineNo();
        List<FuncParamNode> funcParamNodes = new ArrayList<>();
        if (childUnits.get(3).type() == CompileUnit.Type.FuncFParams) {
            funcParamNodes.addAll(childUnits.get(3).childUnits().stream().filter(x -> x.type() == CompileUnit.Type.FuncFParam)
                    .map(SyntaxTreeBuilder::buildFuncParamNode).collect(Collectors.toList()));
        }
        BlockNode funcBlock = buildBlockNode(childUnits.get(childUnits.size() - 1));
        return new FuncDefNode(funcType, funcName, line, funcParamNodes, funcBlock);
    }

    public static DefNode buildDefNode(CompileUnit compileUnit) {
        assert (compileUnit.type() == CompileUnit.Type.ConstDef || compileUnit.type() == CompileUnit.Type.VarDef);
        CompileUnit identUnit = compileUnit.childUnits().get(0);
        List<CompileUnit> childUnit = compileUnit.childUnits();
        List<ExprNode> initValueList = new ArrayList<>();
        List<ExprNode> dim = new ArrayList<>();
        for (int i = 1; i < childUnit.size(); i++) {
            while (childUnit.get(i).type() == CompileUnit.Type.LBRACK) {
                dim.add(buildExprNode(childUnit.get(i + 1)));
                i += 3;
            }
            if (childUnit.get(i).type() == CompileUnit.Type.ASSIGN) {
                Queue<CompileUnit> processQueue = childUnit.get(i + 1).childUnits().stream().
                        filter(x -> x.type() == CompileUnit.Type.InitVal ||
                                x.type() == CompileUnit.Type.ConstExp ||
                                x.type() == CompileUnit.Type.ConstInitVal ||
                                x.type() == CompileUnit.Type.Exp)
                        .collect(Collectors.toCollection(LinkedList::new));
                while (!processQueue.isEmpty()) {
                    CompileUnit u = processQueue.peek();
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
        CompileUnit.Type type = childUnits.get(0).type();
        String paraName = childUnits.get(1).name();
        int line = childUnits.get(1).lineNo();
        List<ExprNode> dimension = new ArrayList<>();
        for (int i = 2; i < childUnits.size(); i++) {
            int dimLayer = 0;
            while (childUnits.get(i).type() == CompileUnit.Type.LBRACK) {
                if (dimLayer == 0) {
                    dimension.add(new NumberNode(0));
                    i += 2;
                    dimLayer += 1;
                } else {
                    dimension.add(buildExprNode(childUnits.get(i + 1)));
                }
            }
        }
        return new FuncParamNode(type, paraName, line, dimension);
    }

    public static BlockNode buildBlockNode(CompileUnit compileUnit) {
        List<CompileUnit> compileUnits = compileUnit.childUnits();
        return new BlockNode(compileUnits.stream().
                filter(x -> x.type() == CompileUnit.Type.BlockItem)
                .map(SyntaxTreeBuilder::buildBlockItemNode).collect(Collectors.toList()));
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
                    return buildContinueStmtNode(compileUnit);
                case BREAKTK:
                    return buildBreakStmtNode(compileUnit);
                case RETURNTK:
                    return buildReturnNode(compileUnit);
                case PRINTFTK:
                    return new PrintfNode(
                            childUnits.get(0).lineNo(),
                            childUnits.get(2).name(),
                            childUnits.stream()
                                    .filter(x -> x.type() == CompileUnit.Type.Exp)
                                    .map(SyntaxTreeBuilder::buildExprNode)
                                    .collect(Collectors.toList())
                    );
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
            return new AssignNode(buildLValNode(childUnit.get(0)),
                    new GetintNode(childUnit.get(2).lineNo()));
        }
        return new AssignNode(buildLValNode(childUnit.get(0)), buildExprNode(childUnit.get(2)));
    }

    public static IfNode buildIfNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnits = compileUnit.childUnits();
        ExprNode cond = buildExprNode(childUnits.get(2));
        StmtNode ifStmt = buildStmtNode(childUnits.get(4));
        if (childUnits.get(5).type() == CompileUnit.Type.ELSETK) {
            StmtNode elseStmt = buildStmtNode(childUnits.get(childUnits.size() - 1));
            return new IfNode(cond, ifStmt, elseStmt);
        }
        return new IfNode(cond, ifStmt);
    }

    public static WhileNode buildWhileNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnits = compileUnit.childUnits();
        ExprNode cond = buildExprNode(childUnits.get(2));
        StmtNode whileStmt = buildStmtNode(childUnits.get(childUnits.size() - 1));
        return new WhileNode(cond, whileStmt);
    }

    public static LValNode buildLValNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnits = compileUnit.childUnits();
        String ident = childUnits.get(0).name();
        int line = childUnits.get(0).lineNo();
        List<ExprNode> indexes = new ArrayList<>();
        for (int i = 1; i < childUnits.size(); i++) {
            while (childUnits.get(i).type() == CompileUnit.Type.LBRACK) {
                indexes.add(buildExprNode(childUnits.get(i + 1)));
                i += 3;
            }
        }
        return new LValNode(ident, line, indexes);
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
                    return new BinaryExpNode(buildExprNode(childUnits.get(0)), childUnits.get(1).type(), buildExprNode(childUnits.get(2)));
                }
            case UnaryExp:
                if (childUnits.size() == 1) {
                    return buildExprNode(childUnits.get(0));
                } else if (childUnits.get(0).type() == CompileUnit.Type.UnaryOp) {
                    return new UnaryExpNode(childUnits.get(0).childUnits().get(0).type(), buildExprNode(childUnits.get(1)));
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
        return new ReturnNode(line, buildExprNode(childUnit.get(1)));
    }

    public static FuncCallNode buildFuncCallNode(CompileUnit compileUnit) {
        List<CompileUnit> childUnit = compileUnit.childUnits();
        String ident = childUnit.get(0).name();
        int line = childUnit.get(0).lineNo();
        List<ExprNode> args = new ArrayList<>();
        if (childUnit.size() > 3 && childUnit.get(2).type() == CompileUnit.Type.FuncRParams) {
            args.addAll(childUnit.get(2).childUnits().stream().
                    filter(x -> x.type() == CompileUnit.Type.Exp).
                    map(SyntaxTreeBuilder::buildExprNode).collect(Collectors.toList()));
        }
        return new FuncCallNode(ident, line, args);
    }
}
