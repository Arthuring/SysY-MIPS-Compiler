package front;

import java.io.LineNumberReader;
import java.util.List;

public class TokenPackage {
    private int pointer;
    private final List<Token> tokenList;
    private Token curToken;

    public TokenPackage(List<Token> tokenList) {
        this.pointer = 0;
        this.tokenList = tokenList;
        this.curToken = null;
        this.next();
    }

    public int getPointer() {
        return this.pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
        curToken = tokenList.get(pointer - 1);
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public Token next() {
        if (pointer == tokenList.size()) {
            return null;
        }
        curToken = tokenList.get(pointer);
        pointer += 1;
        return tokenList.get(pointer - 1);
    }

    public Token preview(int offset) {
        if (pointer + offset > tokenList.size()) {
            return null;
        }
        return tokenList.get(pointer + offset - 1);
    }

    public Token getCurToken() {
        return curToken;
    }

    public Token previous() {
        if (pointer == 1) {
            return null;
        }
        pointer -= 1;
        this.curToken = tokenList.get(pointer - 1);
        return tokenList.get(pointer - 1);
    }

    public Token get(int index) {
        if (index < tokenList.size()) {
            return tokenList.get(index);
        } else {
            return null;
        }
    }

    public int sizeRemain() {
        return this.tokenList.size() - pointer;
    }
}
