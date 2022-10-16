package front;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static int position = 0;
    public static int line = 1;
    public static Set<String> whiteSpace = new HashSet<String>() {
        {
            add(" ");
            add("\n");
            add("\r");
            add("\t");
        }
    };
    public static Map<String, Token.Type> keyWords = new HashMap<String, Token.Type>() {
        {
            put("main", Token.Type.MAINTK);
            put("int", Token.Type.INTTK);
            put("if", Token.Type.IFTK);
            put("while", Token.Type.WHILETK);
            put("break", Token.Type.BREAKTK);
            put("continue", Token.Type.CONTINUETK);
            put("return", Token.Type.RETURNTK);
            put("void", Token.Type.VOIDTK);
            put("const", Token.Type.CONSTTK);
            put("getint", Token.Type.GETINTTK);
            put("printf", Token.Type.PRINTFTK);
        }
    };

    public static Map<String, Token.Type> singleCharTk = new HashMap<String, Token.Type>() {
        {
            put("+", Token.Type.PLUS);
            put("-", Token.Type.MINU);
            put("*", Token.Type.MULT);
            put("/", Token.Type.DIV);
            put("%", Token.Type.MOD);
            put(";", Token.Type.SEMICN);
            put(",", Token.Type.COMMA);
            put("(", Token.Type.LPARENT);
            put(")", Token.Type.RPARENT);
            put("[", Token.Type.LBRACK);
            put("]", Token.Type.RBRACK);
            put("{", Token.Type.LBRACE);
            put("}", Token.Type.RBRACE);
        }
    };

    public static List<Token> tokenize(String sourceCode) throws Exception {
        final List<Token> tokens = new LinkedList<>();
        Pattern pattern = Token.TOKEN_PATTERN;
        Matcher matcher = pattern.matcher(sourceCode);
        int cntLine = 1;
        lab:
        while (matcher.find()) {
            for (Token.Type t : Token.Type.values()) {
                if (matcher.group(t.toString()) != null) {
                    String stringInfo = matcher.group(t.toString());
                    cntLine += stringInfo.chars().boxed().filter(ch -> ch == '\n').count();
                    if (!Token.eat(t)) {
                        tokens.add(new Token(cntLine, t, stringInfo));
                    }
                    continue lab;
                }
            }
            throw new Exception("Illegal");
        }
        return tokens;
    }

    public static List<Token> tokenizeAutomata(String sourceCode) throws Exception {
        final List<Token> tokens = new LinkedList<>();
        String sourceCodeUncomment = uncomment(sourceCode);
        Token token = getToken(sourceCodeUncomment);
        while (token != null) {
            tokens.add(token);
            token = getToken(sourceCodeUncomment);
        }
        return tokens;
    }

    public static String uncomment(String sourceCode) {
        StringBuilder sb = new StringBuilder(sourceCode);
        for (int i = 0; i < sourceCode.length(); ) {
            if (sourceCode.charAt(i) == '"') {
                do {
                    i += 1;
                } while (sourceCode.charAt(i) != '"');
                i += 1;
            } else if (sourceCode.charAt(i) == '/' &&
                    i + 1 < sourceCode.length() &&
                    sourceCode.charAt(i + 1) == '/') {
                sb.replace(i, i + 2, "  ");
                i += 2;
                while (sourceCode.charAt(i) != '\n') {
                    sb.replace(i, i + 1, " ");
                    i += 1;
                }
                i += 1;
            } else if (sourceCode.charAt(i) == '/' &&
                    i + 1 < sourceCode.length() &&
                    sourceCode.charAt(i + 1) == '*') {
                sb.replace(i, i + 2, "  ");
                i += 2;
                while (sourceCode.charAt(i) != '*' || sourceCode.charAt(i + 1) != '/') {
                    sb.replace(i, i + 1, sourceCode.charAt(i) == '\n' ? "\n" : " ");
                    i += 1;
                }
                sb.replace(i, i + 2, "  ");
                i += 2;
            } else {
                i += 1;
            }
        }
        return sb.toString();
    }

    public static Token getToken(String sourceCode) {
        if (position == sourceCode.length()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (position < sourceCode.length()
                && whiteSpace.contains(String.valueOf(sourceCode.charAt(position)))
        ) {
            if (sourceCode.charAt(position) == '\n') {
                line += 1;
            }
            position += 1;
        }
        if (position == sourceCode.length()) {
            return null;
        }

        if (Character.isLetter(sourceCode.charAt(position)) || sourceCode.charAt(position) == '_') {
            do {
                stringBuilder.append(String.valueOf(sourceCode.charAt(position)));
                position += 1;
            } while (Character.isLetter(sourceCode.charAt(position)) ||
                    Character.isDigit(sourceCode.charAt(position)) ||
                    sourceCode.charAt(position) == '_');
            return new Token(line, keyWords.getOrDefault(stringBuilder.toString(), Token.Type.IDENFR), stringBuilder.toString());
        } else if (Character.isDigit(sourceCode.charAt(position))) {
            do {
                stringBuilder.append(sourceCode.charAt(position));
                position += 1;
            } while (Character.isDigit(sourceCode.charAt(position)));
            return new Token(line, Token.Type.INTCON, stringBuilder.toString());
        } else if (sourceCode.charAt(position) == '"') {
            do {
                stringBuilder.append(String.valueOf(sourceCode.charAt(position)));
                position += 1;
            } while (sourceCode.charAt(position) != '"');
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            return new Token(line, Token.Type.STRCON, stringBuilder.toString());
        } else if (sourceCode.charAt(position) == '!') {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            if (sourceCode.charAt(position) != '=') {
                return new Token(line, Token.Type.NOT, stringBuilder.toString());
            } else {
                stringBuilder.append(sourceCode.charAt(position));
                position += 1;
                return new Token(line, Token.Type.NEQ, stringBuilder.toString());
            }
        } else if (sourceCode.charAt(position) == '&') {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            return new Token(line, Token.Type.AND, stringBuilder.toString());
        } else if (sourceCode.charAt(position) == '|') {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            return new Token(line, Token.Type.OR, stringBuilder.toString());
        } else if (sourceCode.charAt(position) == '<') {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            if (sourceCode.charAt(position) != '=') {
                return new Token(line, Token.Type.LSS, stringBuilder.toString());
            } else {
                stringBuilder.append(sourceCode.charAt(position));
                position += 1;
                return new Token(line, Token.Type.LEQ, stringBuilder.toString());
            }
        } else if (sourceCode.charAt(position) == '>') {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            if (sourceCode.charAt(position) != '=') {
                return new Token(line, Token.Type.GRE, stringBuilder.toString());
            } else {
                stringBuilder.append(sourceCode.charAt(position));
                position += 1;
                return new Token(line, Token.Type.GEQ, stringBuilder.toString());
            }
        } else if (sourceCode.charAt(position) == '=') {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            if (sourceCode.charAt(position) != '=') {
                return new Token(line, Token.Type.ASSIGN, stringBuilder.toString());
            } else {
                stringBuilder.append(sourceCode.charAt(position));
                position += 1;
                return new Token(line, Token.Type.EQL, stringBuilder.toString());
            }
        } else if (singleCharTk.containsKey(String.valueOf(sourceCode.charAt(position)))) {
            stringBuilder.append(sourceCode.charAt(position));
            position += 1;
            return new Token(line, singleCharTk.get(stringBuilder.toString()), stringBuilder.toString());
        } else {
            return null;
        }
    }
}
