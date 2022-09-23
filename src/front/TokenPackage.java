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
        curToken = tokenList.get(pointer);
        pointer += 1;
    }

    public Token preview(int offset){
        if(pointer + offset > tokenList.size()){
            return null;
        }
        return tokenList.get(pointer + offset - 1);
    }

    public Token getCurToken() {
        return curToken;
    }

    public Token get(int index) {
        if (index < tokenList.size()) {
            return tokenList.get(index);
        } else {
            return null;
        }
    }
}
