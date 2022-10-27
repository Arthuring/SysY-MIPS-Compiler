package front.nodes;

import front.SymbolTable;
import front.TableEntry;

public abstract class ExprNode implements StmtNode {
    public TableEntry.ValueType valueType;
    public int dimension;

    public abstract ExprNode simplify(SymbolTable symbolTable);
}
