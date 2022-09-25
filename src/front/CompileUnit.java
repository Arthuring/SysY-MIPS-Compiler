package front;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CompileUnit {
    private final String name;
    private final Type type;
    private final List<CompileUnit> childUnits;
    private final boolean isEnd;

    public enum Type {
        CompUnit, Decl, ConstDecl, BType, ConstDef,
        ConstInitVal, VarDecl, VarDef, FuncDef, MainFuncDef, FuncType,
        FuncFParams, FuncFParam, Block, BlockItem, Stmt, Exp, Cond,
        LVal, PrimaryExp, Number, UnaryExp,
        InitVal,
        UnaryOp, FuncRParams, MulExp, AddExp, RelExp, EqExp, LAndExp, LOrExp, ConstExp,
        MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK,
        IFTK, ELSETK,
        WHILETK,
        GETINTTK, PRINTFTK,
        RETURNTK, VOIDTK,
        SINGLECOMMENT, MULTICOMMENT,
        PLUS, MINU,
        MULT, DIV, MOD,
        LEQ, GEQ, EQL, NEQ,
        AND, OR,
        NOT, LSS, GRE, ASSIGN,
        SEMICN, COMMA,
        LPARENT, RPARENT,
        LBRACK, RBRACK,
        LBRACE, RBRACE,
        IDENFR, INTCON, STRCON,
        WHITESPACE
    }

    public CompileUnit(String name, List<CompileUnit> unitList, Type type, boolean isEnd) {
        this.name = name;
        this.type = type;
        this.childUnits = unitList;
        this.isEnd = isEnd;
    }

    @Override
    public String toString() {
        if (this.isEnd) {
            return type.toString() + " " + name;
        } else {
            String s = childUnits.stream().map(CompileUnit::toString)
                    .collect(Collectors.joining("\n"));
            if (new HashSet<>(Arrays.asList(Type.BType, Type.Decl, Type.BlockItem)).contains(this.type)) {
                return s;
            } else {
                if (!s.equals("")) {
                    return s + "\n<" + type.toString() + ">";
                } else {
                    return s + "<" + type.toString() + ">";
                }
            }

        }
    }
}
