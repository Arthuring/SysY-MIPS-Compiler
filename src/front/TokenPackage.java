package front;

import java.util.List;

public class TokenPackage {
    private int pointer;
    private final List<Token> tokenList;
    private Token.Type curToken;
    private String curInfo;

    public TokenPackage(List<Token> tokenList) {
        this.pointer = 0;
        this.tokenList = tokenList;
        this.curInfo = null;
        this.curToken = null;
    }

    public int getPointer() {
        return this.pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void next() {
        if (pointer == tokenList.size()) {
            return;
        }
        curToken = tokenList.get(pointer).type();
        curInfo = tokenList.get(pointer).stringInfo();
        pointer += 1;
    }

    public String getCurInfo() {
        return curInfo;
    }

    public Token.Type getCurToken() {
        return curToken;
    }
}
