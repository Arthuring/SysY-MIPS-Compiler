package front.nodes;

import front.SymbolTable;

import java.util.ArrayList;

public class IfNode implements StmtNode {
    private ExprNode cond;
    private final BlockNode ifStmt;
    private final BlockNode elseStmt;

    public IfNode(ExprNode cond, BlockNode ifStmt, BlockNode elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
    }

    public IfNode(ExprNode cond, BlockNode ifStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = null;
    }

    public ExprNode cond() {
        return cond;
    }

    public BlockNode ifStmt() {
        return ifStmt;
    }

    public BlockNode elseStmt() {
        return elseStmt;
    }

    public void setCond(ExprNode cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        return "IfNode{\n" +
                "cond=" + cond +
                ",\n ifStmt=" + ifStmt +
                ",\n elseStmt=" + elseStmt +
                "\n}";
    }


}
