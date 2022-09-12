package front;

import java.util.regex.Pattern;

public class Token {
    public enum Type {
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

    public static final String MAINTK_P = "(?<MAINTK>main(?![a-zA-Z0-9_]))";
    public static final String CONSTTK_P = "(?<CONSTTK>const(?![a-zA-Z0-9_]))";
    public static final String INTTK_P = "(?<INTTK>int(?![a-zA-Z0-9_]))";
    public static final String BREAKTK_P = "(?<BREAKTK>break(?![a-zA-Z0-9_]))";
    public static final String CONTINUETK_P = "(?<CONTINUETK>continue(?![a-zA-Z0-9_]))";
    public static final String IFTK_P = "(?<IFTK>if(?![a-zA-Z0-9_]))";
    public static final String ELSETK_P = "(?<ELSETK>else(?![a-zA-Z0-9_]))";
    public static final String WHILETK_P = "(?<WHILETK>while(?![a-zA-Z0-9_]))";
    public static final String GETINTTK_P = "(?<GETINTTK>getint(?![a-zA-Z0-9_]))";
    public static final String PRINTFTK_P = "(?<PRINTFTK>printf(?![a-zA-Z0-9_]))";
    public static final String RETURNTK_P = "(?<RETURNTK>return(?![a-zA-Z0-9_]))";
    public static final String VOIDTK_P = "(?<VOIDTK>void(?![a-zA-Z0-9_]))";
    public static final String SINGLECOMMENT_P = "(?<SINGLECOMMENT>//.*)";
    public static final String MULTICOMMENT_P = "(?<MULTICOMMENT>/\\*[\\s\\S]*?\\*/)";
    public static final String PLUS_P = "(?<PLUS>\\+)";
    public static final String MINU_P = "(?<MINU>-)";
    public static final String MULT_P = "(?<MULT>\\*)";
    public static final String DIV_P = "(?<DIV>/)";
    public static final String MOD_P = "(?<MOD>%)";
    public static final String LEQ_P = "(?<LEQ><=)";
    public static final String GEQ_P = "(?<GEQ>>=)";
    public static final String EQL_P = "(?<EQL>==)";
    public static final String NEQ_P = "(?<NEQ>!=)";
    public static final String AND_P = "(?<AND>&&)";
    public static final String OR_P = "(?<OR>\\|\\|)";
    public static final String NOT_P = "(?<NOT>!)";
    public static final String LSS_P = "(?<LSS><)";
    public static final String GRE_P = "(?<GRE>>)";
    public static final String ASSIGN_P = "(?<ASSIGN>=)";
    public static final String SEMICN_P = "(?<SEMICN>;)";
    public static final String COMMA_P = "(?<COMMA>,)";
    public static final String LPARENT_P = "(?<LPARENT>\\()";
    public static final String RPARENT_P = "(?<RPARENT>\\))";
    public static final String LBRACK_P = "(?<LBRACK>\\[)";
    public static final String RBRACK_P = "(?<RBRACK>])";
    public static final String LBRACE_P = "(?<LBRACE>\\{)";
    public static final String RBRACE_P = "(?<RBRACE>})";
    public static final String IDENFR_P = "(?<IDENFR>[a-zA-Z_][a-zA-Z_0-9]*)";
    public static final String INTCON_P = "(?<INTCON>[1-9][0-9]*|0)";
    public static final String STRCON_P = "(?<STRCON>\".*?\")";
    public static final String WHITESPACE_P = "(?<WHITESPACE>\\s+)";

    public static final Pattern TOKEN_PATTERN = Pattern.compile(
            MAINTK_P + "|" +
                    CONSTTK_P + "|" +
                    INTTK_P + "|" +
                    BREAKTK_P + "|" +
                    CONTINUETK_P + "|" +
                    IFTK_P + "|" +
                    ELSETK_P + "|" +
                    WHILETK_P + "|" +
                    GETINTTK_P + "|" +
                    PRINTFTK_P + "|" +
                    RETURNTK_P + "|" +
                    VOIDTK_P + "|" +
                    SINGLECOMMENT_P + "|" +
                    MULTICOMMENT_P + "|" +
                    PLUS_P + "|" +
                    MINU_P + "|" +
                    MULT_P + "|" +
                    DIV_P + "|" +
                    MOD_P + "|" +
                    LEQ_P + "|" +
                    GEQ_P + "|" +
                    EQL_P + "|" +
                    NEQ_P + "|" +
                    AND_P + "|" +
                    OR_P + "|" +
                    NOT_P + "|" +
                    LSS_P + "|" +
                    GRE_P + "|" +
                    ASSIGN_P + "|" +
                    SEMICN_P + "|" +
                    COMMA_P + "|" +
                    LPARENT_P + "|" +
                    RPARENT_P + "|" +
                    LBRACK_P + "|" +
                    RBRACK_P + "|" +
                    LBRACE_P + "|" +
                    RBRACE_P + "|" +
                    IDENFR_P + "|" +
                    INTCON_P + "|" +
                    STRCON_P + "|" +
                    WHITESPACE_P + "|" +
                    "(?<ERR>.)"

    );

    public static boolean eat(Type type) {
        return type == Type.WHITESPACE || type == Type.SINGLECOMMENT || type == Type.MULTICOMMENT;
    }

    private final String stringInfo;
    private final Type type;
    private final int line;

    public Token(int line, Type type, String stringInfo) {
        this.stringInfo = stringInfo;
        this.type = type;
        this.line = line;
    }

    public int line() {
        return line;
    }

    public Type type() {
        return type;
    }

    public String stringInfo() {
        return stringInfo;
    }

    public String toString() {
        return this.type.toString() + " " + this.stringInfo;
    }

}
