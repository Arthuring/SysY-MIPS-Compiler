package front.nodes;

import front.TableEntry;

public abstract class ExprNode implements StmtNode {
    public TableEntry.ValueType valueType;
    public int dimension;
}
