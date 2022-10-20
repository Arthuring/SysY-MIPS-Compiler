package front;

import exception.CompileExc;

import java.util.*;

public class Parser {
    public static final List<CompileExc> COMPILE_EXCS = new ArrayList<>();

    public static CompileUnit endUnitBuilder(TokenPackage tokenPackage, Token.Type type) {
        if (tokenPackage.getCurToken().type() == type) {
            CompileUnit compileUnit = new CompileUnit(tokenPackage.getCurToken().stringInfo(),
                    null, CompileUnit.Type.valueOf(type.toString()), true, tokenPackage.getCurToken().line());
            tokenPackage.next();
            return compileUnit;
        } else {
            if (type == Token.Type.RPARENT) {
                COMPILE_EXCS.add(new CompileExc(CompileExc.ErrType.EXPECTED_PARENT, tokenPackage.preview(-1).line()));
                return new CompileUnit(")",
                        null, CompileUnit.Type.RPARENT, true, tokenPackage.preview(-1).line());
            } else if (type == Token.Type.RBRACK) {
                COMPILE_EXCS.add(new CompileExc(CompileExc.ErrType.EXPECTED_BRACK, tokenPackage.preview(-1).line()));
                return new CompileUnit("]",
                        null, CompileUnit.Type.RBRACK, true, tokenPackage.preview(-1).line());
            } else if (type == Token.Type.SEMICN) {
                COMPILE_EXCS.add(new CompileExc(CompileExc.ErrType.EXPECTED_SEMICN, tokenPackage.preview(-1).line()));
                return new CompileUnit(";", null, CompileUnit.Type.SEMICN, true, tokenPackage.preview(-1).line());
            }
            return null;
            //else throw exception
        }
    }

    public static CompileUnit endUnitBuilder(TokenPackage tokenPackage, Set<Token.Type> typeSet) {
        if (typeSet.contains(tokenPackage.getCurToken().type())) {
            CompileUnit compileUnit = new CompileUnit(tokenPackage.getCurToken().stringInfo(), null,
                    CompileUnit.Type.valueOf(tokenPackage.getCurToken().type().toString()),
                    true, tokenPackage.getCurToken().line());
            tokenPackage.next();
            return compileUnit;
        } else {
            return null;
            //else throw exception
        }
    }

    public static CompileUnit parseCompUnit(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        while (tokenPackage.sizeRemain() >= 3 &&
                (tokenPackage.preview(0).type() == Token.Type.CONSTTK
                        || tokenPackage.preview(0).type() == Token.Type.INTTK &&
                        tokenPackage.preview(1).type() == Token.Type.IDENFR &&
                        tokenPackage.preview(2).type() != Token.Type.LPARENT)) {
            childUnit.add(parseDecl(tokenPackage));
        }

        while (tokenPackage.sizeRemain() >= 3
                && (tokenPackage.preview(0).type() == Token.Type.VOIDTK
                || tokenPackage.preview(0).type() == Token.Type.INTTK &&
                tokenPackage.preview(1).type() != Token.Type.MAINTK)
        ) {
            childUnit.add(parseFuncDef(tokenPackage));
        }
        childUnit.add(parseMainFuncDef(tokenPackage));
        return new CompileUnit("CompUnit", childUnit, CompileUnit.Type.CompUnit, false);
    }

    public static CompileUnit parseDecl(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.CONSTTK) {
            childUnit.add(parseConstDecl(tokenPackage));
        } else if (tokenPackage.getCurToken().type() == Token.Type.INTTK) {
            childUnit.add(parseVarDecl(tokenPackage));
        }

        return new CompileUnit("Decl", childUnit, CompileUnit.Type.Decl, false);
    }

    public static CompileUnit parseVarDecl(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseBType(tokenPackage));
        childUnit.add(parseVarDef(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
            childUnit.add(parseVarDef(tokenPackage));
        }
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
        return new CompileUnit("VarDecl", childUnit, CompileUnit.Type.VarDecl, false);
    }

    public static CompileUnit parseFuncDef(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseFuncType(tokenPackage));
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IDENFR));
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
        if (tokenPackage.getCurToken().type() == Token.Type.INTTK) {
            childUnit.add(parseFuncFParams(tokenPackage));
        }
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
        childUnit.add(parseBlock(tokenPackage));
        return new CompileUnit("FuncDef", childUnit, CompileUnit.Type.FuncDef, false);
    }

    public static CompileUnit parseMainFuncDef(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.INTTK));
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.MAINTK));
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
        childUnit.add(parseBlock(tokenPackage));
        return new CompileUnit("MainFuncDef", childUnit, CompileUnit.Type.MainFuncDef, false);
    }

    public static CompileUnit parseConstDecl(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.CONSTTK));
        childUnit.add(parseBType(tokenPackage));
        childUnit.add(parseConstDef(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
            childUnit.add(parseConstDef(tokenPackage));
        }
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
        return new CompileUnit("ConstDecl", childUnit, CompileUnit.Type.ConstDecl, false);
    }

    public static CompileUnit parseBType(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.INTTK));
        return new CompileUnit("BType", childUnit, CompileUnit.Type.BType, false);
    }

    public static CompileUnit parseConstDef(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IDENFR));//Ident
        while (tokenPackage.getCurToken().type() == Token.Type.LBRACK) { //{[ConstExp]}
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACK));
            childUnit.add(parseConstExp(tokenPackage));
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACK));
        }
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.ASSIGN)); //=
        childUnit.add(parseConstInitVal(tokenPackage));
        return new CompileUnit("ConstDef", childUnit, CompileUnit.Type.ConstDef, false);
    }

    public static CompileUnit parseConstExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseAddExp(tokenPackage));
        return new CompileUnit("ConstExp", childUnit, CompileUnit.Type.ConstExp, false);
    }

    public static CompileUnit parseConstInitVal(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.LBRACE) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACE));
            if (tokenPackage.getCurToken().type() != Token.Type.RBRACE) {
                childUnit.add(parseConstInitVal(tokenPackage));
                while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
                    childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
                    childUnit.add(parseConstInitVal(tokenPackage));
                }
            }
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACE));
        } else {
            childUnit.add(parseConstExp(tokenPackage));
        }
        return new CompileUnit("ConstInitVal", childUnit, CompileUnit.Type.ConstInitVal, false);
    }

    public static CompileUnit parseVarDef(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IDENFR));
        while (tokenPackage.getCurToken().type() == Token.Type.LBRACK) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACK));
            childUnit.add(parseConstExp(tokenPackage));
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACK));
        }
        if (tokenPackage.getCurToken().type() == Token.Type.ASSIGN) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.ASSIGN));
            childUnit.add(parseInitVal(tokenPackage));
        }
        return new CompileUnit("VarDef", childUnit, CompileUnit.Type.VarDef, false);
    }

    public static CompileUnit parseInitVal(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.LBRACE) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACE));
            if (tokenPackage.getCurToken().type() != Token.Type.RBRACE) {
                childUnit.add(parseInitVal(tokenPackage));
                while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
                    childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
                    childUnit.add(parseInitVal(tokenPackage));
                }
            }
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACE));
        } else {
            childUnit.add(parseExp(tokenPackage));
        }
        return new CompileUnit("InitVal", childUnit, CompileUnit.Type.InitVal, false);
    }

    public static CompileUnit parseExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseAddExp(tokenPackage));
        return new CompileUnit("Exp", childUnit, CompileUnit.Type.Exp, false);
    }

    public static CompileUnit parseFuncType(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.INTTK) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.INTTK));
        } else {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.VOIDTK));
        }
        return new CompileUnit("FuncType", childUnit, CompileUnit.Type.FuncType, false);
    }

    public static CompileUnit parseFuncFParams(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseFuncFParam(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
            childUnit.add(parseFuncFParam(tokenPackage));
        }
        return new CompileUnit("FuncFParams", childUnit, CompileUnit.Type.FuncFParams, false);
    }

    public static CompileUnit parseFuncFParam(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseBType(tokenPackage));
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IDENFR));
        if (tokenPackage.getCurToken().type() == Token.Type.LBRACK) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACK));
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACK));
            while (tokenPackage.getCurToken().type() == Token.Type.LBRACK) {
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACK));
                childUnit.add(parseConstExp(tokenPackage));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACK));
            }
        }
        return new CompileUnit("FuncFParam", childUnit, CompileUnit.Type.FuncFParam, false);
    }

    public static CompileUnit parseBlock(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACE));
        while (tokenPackage.getCurToken().type() != Token.Type.RBRACE) {
            childUnit.add(parseBlockItem(tokenPackage));
        }
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACE));
        return new CompileUnit("Block", childUnit, CompileUnit.Type.Block, false);
    }

    public static CompileUnit parseBlockItem(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.CONSTTK ||
                tokenPackage.getCurToken().type() == Token.Type.INTTK) {
            childUnit.add(parseDecl(tokenPackage));
        } else {
            childUnit.add(parseStmt(tokenPackage));
        }
        return new CompileUnit("BlockItem", childUnit, CompileUnit.Type.BlockItem, false);
    }

    public static CompileUnit parseStmt(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        switch (tokenPackage.getCurToken().type()) {
            case LBRACE:
                childUnit.add(parseBlock(tokenPackage));
                break;
            case IFTK:
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IFTK));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
                childUnit.add(parseCond(tokenPackage));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
                childUnit.add(parseStmt(tokenPackage));
                if (tokenPackage.getCurToken().type() == Token.Type.ELSETK) {
                    childUnit.add(endUnitBuilder(tokenPackage, Token.Type.ELSETK));
                    childUnit.add(parseStmt(tokenPackage));
                }
                break;
            case WHILETK:
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.WHILETK));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
                childUnit.add(parseCond(tokenPackage));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
                childUnit.add(parseStmt(tokenPackage));
                break;
            case BREAKTK:
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.BREAKTK));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                break;
            case CONTINUETK:
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.CONTINUETK));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                break;
            case RETURNTK:
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RETURNTK));
                if (tokenPackage.getCurToken().type() == Token.Type.INTCON ||
                        tokenPackage.getCurToken().type() == Token.Type.IDENFR ||
                        tokenPackage.getCurToken().type() == Token.Type.PLUS ||
                        tokenPackage.getCurToken().type() == Token.Type.MINU) {
                    childUnit.add(parseExp(tokenPackage));
                }
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                break;
            case PRINTFTK:
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.PRINTFTK));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.STRCON));
                while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
                    childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
                    childUnit.add(parseExp(tokenPackage));
                }
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                break;
            default:
                boolean isExp = true;
                if (tokenPackage.getCurToken().type() == Token.Type.IDENFR) {
                    //preview of Lval = exp
                    int pointer = tokenPackage.getPointer();
                    parseLVal(tokenPackage);
                    if (tokenPackage.getCurToken().type() == Token.Type.ASSIGN) {
                        isExp = false;
                    }
                    tokenPackage.setPointer(pointer);
                }
                if (isExp) {
                    if (tokenPackage.getCurToken().type() != Token.Type.SEMICN) {
                        childUnit.add(parseExp(tokenPackage));
                    }
                    childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                } else {
                    childUnit.add(parseLVal(tokenPackage));
                    childUnit.add(endUnitBuilder(tokenPackage, Token.Type.ASSIGN));
                    if (tokenPackage.getCurToken().type() == Token.Type.GETINTTK) {
                        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.GETINTTK));
                        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
                        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
                        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                    } else {
                        childUnit.add(parseExp(tokenPackage));
                        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.SEMICN));
                    }
                }
        }
        return new CompileUnit("Stmt", childUnit, CompileUnit.Type.Stmt, false);
    }

    public static CompileUnit parseLVal(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IDENFR));
        while (tokenPackage.getCurToken().type() == Token.Type.LBRACK) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LBRACK));
            childUnit.add(parseExp(tokenPackage));
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RBRACK));
        }
        return new CompileUnit("LVal", childUnit, CompileUnit.Type.LVal, false);
    }

    public static CompileUnit parseCond(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseLOrExp(tokenPackage));
        return new CompileUnit("Cond", childUnit, CompileUnit.Type.Cond, false);
    }

    public static CompileUnit parseAddExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseMulExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.PLUS || tokenPackage.getCurToken().type() == Token.Type.MINU) {
            //            childUnit.add(new CompileUnit("AddExp", new ArrayList<>(), CompileUnit.Type.AddExp, false));
            //            childUnit.add(endUnitBuilder(tokenPackage, tokenPackage.getCurToken().type()));
            //            childUnit.add(parseMulExp(tokenPackage));
            List<CompileUnit> newChild = new ArrayList<>(childUnit);
            CompileUnit newLayer = new CompileUnit("AddExp", newChild, CompileUnit.Type.AddExp, false);
            childUnit.clear();
            childUnit.add(newLayer);
            childUnit.add(endUnitBuilder(tokenPackage, tokenPackage.getCurToken().type()));
            childUnit.add(parseMulExp(tokenPackage));
        }
        return new CompileUnit("AddExp", childUnit, CompileUnit.Type.AddExp, false);
    }

    public static CompileUnit parseLOrExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseLAndExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.OR) {
            List<CompileUnit> newChild = new ArrayList<>(childUnit);
            CompileUnit newLayer = new CompileUnit("LOrExp", newChild, CompileUnit.Type.LOrExp, false);
            childUnit.clear();
            childUnit.add(newLayer);
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.OR));
            childUnit.add(parseLAndExp(tokenPackage));
        }
        return new CompileUnit("LOrExp", childUnit, CompileUnit.Type.LOrExp, false);
    }

    public static CompileUnit parsePrimaryExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.LPARENT) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
            childUnit.add(parseExp(tokenPackage));
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
        } else if (tokenPackage.getCurToken().type() == Token.Type.INTCON) {
            childUnit.add(parseNumber(tokenPackage));
        } else {
            childUnit.add(parseLVal(tokenPackage));
        }
        return new CompileUnit("PrimaryExp", childUnit, CompileUnit.Type.PrimaryExp, false);
    }

    public static CompileUnit parseNumber(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, Token.Type.INTCON));
        return new CompileUnit("Number", childUnit, CompileUnit.Type.Number, false);
    }

    public static CompileUnit parseUnaryExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        if (tokenPackage.getCurToken().type() == Token.Type.PLUS ||
                tokenPackage.getCurToken().type() == Token.Type.MINU ||
                tokenPackage.getCurToken().type() == Token.Type.NOT) {
            childUnit.add(parseUnaryOp(tokenPackage));
            childUnit.add(parseUnaryExp(tokenPackage));
        } else {
            if (tokenPackage.sizeRemain() >= 2 && (tokenPackage.getCurToken().type() == Token.Type.IDENFR)
                    && tokenPackage.preview(1).type() == Token.Type.LPARENT) {
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.IDENFR));
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.LPARENT));
                if (tokenPackage.getCurToken().type() == Token.Type.INTCON ||
                tokenPackage.getCurToken().type() == Token.Type.IDENFR ||
                tokenPackage.getCurToken().type() == Token.Type.PLUS ||
                tokenPackage.getCurToken().type() == Token.Type.MINU) {
                    childUnit.add(parseFuncRParams(tokenPackage));
                }
                childUnit.add(endUnitBuilder(tokenPackage, Token.Type.RPARENT));
            } else {
                childUnit.add(parsePrimaryExp(tokenPackage));
            }
        }
        return new CompileUnit("UnaryExp", childUnit, CompileUnit.Type.UnaryExp, false);
    }

    public static CompileUnit parseUnaryOp(TokenPackage tokenPackage) {

        Set<Token.Type> typeSet = new HashSet<>();
        typeSet.add(Token.Type.PLUS);
        typeSet.add(Token.Type.MINU);
        typeSet.add(Token.Type.NOT);
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(endUnitBuilder(tokenPackage, typeSet));
        return new CompileUnit("UnaryOp", childUnit, CompileUnit.Type.UnaryOp, false);
    }

    public static CompileUnit parseFuncRParams(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.COMMA) {
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.COMMA));
            childUnit.add(parseExp(tokenPackage));
        }
        return new CompileUnit("FuncRParams", childUnit, CompileUnit.Type.FuncRParams, false);
    }

    public static CompileUnit parseMulExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseUnaryExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.MULT ||
                tokenPackage.getCurToken().type() == Token.Type.DIV ||
                tokenPackage.getCurToken().type() == Token.Type.MOD) {
            List<CompileUnit> newChild = new ArrayList<>(childUnit);
            CompileUnit newLayer = new CompileUnit("MulExp", newChild, CompileUnit.Type.MulExp, false);
            childUnit.clear();
            childUnit.add(newLayer);
            childUnit.add(endUnitBuilder(tokenPackage,
                    new HashSet<>(Arrays.asList(Token.Type.MULT, Token.Type.DIV, Token.Type.MOD))));
            childUnit.add(parseUnaryExp(tokenPackage));
        }
        return new CompileUnit("MulExp", childUnit, CompileUnit.Type.MulExp, false);
    }

    public static CompileUnit parseRelExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseAddExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.LSS ||
                tokenPackage.getCurToken().type() == Token.Type.GRE ||
                tokenPackage.getCurToken().type() == Token.Type.GEQ ||
                tokenPackage.getCurToken().type() == Token.Type.LEQ) {
            List<CompileUnit> newChild = new ArrayList<>(childUnit);
            CompileUnit newLayer = new CompileUnit("RelExp", newChild, CompileUnit.Type.RelExp, false);
            childUnit.clear();
            childUnit.add(newLayer);

            childUnit.add(endUnitBuilder(tokenPackage, new HashSet<>(Arrays.asList(Token.Type.LSS, Token.Type.GRE, Token.Type.GEQ, Token.Type.LEQ))));
            childUnit.add(parseAddExp(tokenPackage));
        }
        return new CompileUnit("RelExp", childUnit, CompileUnit.Type.RelExp, false);
    }

    public static CompileUnit parseEqExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseRelExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.EQL ||
                tokenPackage.getCurToken().type() == Token.Type.NEQ) {
            List<CompileUnit> newChild = new ArrayList<>(childUnit);
            CompileUnit newLayer = new CompileUnit("EqExp", newChild, CompileUnit.Type.EqExp, false);
            childUnit.clear();
            childUnit.add(newLayer);
            childUnit.add(endUnitBuilder(tokenPackage, new HashSet<>(Arrays.asList(Token.Type.EQL, Token.Type.NEQ))));
            childUnit.add(parseRelExp(tokenPackage));
        }
        return new CompileUnit("EqExp", childUnit, CompileUnit.Type.EqExp, false);
    }

    public static CompileUnit parseLAndExp(TokenPackage tokenPackage) {
        List<CompileUnit> childUnit = new ArrayList<>();
        childUnit.add(parseEqExp(tokenPackage));
        while (tokenPackage.getCurToken().type() == Token.Type.AND) {
            List<CompileUnit> newChild = new ArrayList<>(childUnit);
            CompileUnit newLayer = new CompileUnit("LAndExp", newChild, CompileUnit.Type.LAndExp, false);
            childUnit.clear();
            childUnit.add(newLayer);
            childUnit.add(endUnitBuilder(tokenPackage, Token.Type.AND));
            childUnit.add(parseEqExp(tokenPackage));
        }
        return new CompileUnit("LAndExp", childUnit, CompileUnit.Type.LAndExp, false);
    }
}
