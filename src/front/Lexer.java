package front;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static List<Token> tokenize(String sourceCode) throws Exception {
        final List<Token> tokens = new LinkedList<>();
        Pattern pattern = Token.TOKEN_PATTERN;
        Matcher matcher = pattern.matcher(sourceCode);
        int cntLine = 1;
        lab:
        while (matcher.find()) {
            for (Token.Type t : Token.Type.values()) {
                if (matcher.group(t.toString()) != null) {
                    String stringInfo = matcher.group(t.toString()).trim();
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
}
