package front.nodes;

import front.SymbolTable;
import front.TableEntry;

import java.util.ArrayList;
import java.util.List;

public class LValNode extends ExprNode {
    private final String ident;
    private final int line;
    private final List<ExprNode> index;

    public LValNode(String ident, int line, List<ExprNode> index, int dimension, TableEntry.ValueType valueType) {
        super.dimension = dimension - index.size();
        super.valueType = valueType;
        this.ident = ident;
        this.line = line;
        this.index = index;
    }

    public LValNode(int dimension, TableEntry.ValueType valueType, String ident, int line, List<ExprNode> index) {
        super.dimension = dimension;
        super.valueType = valueType;
        this.ident = ident;
        this.line = line;
        this.index = index;
    }

    public int line() {
        return line;
    }

    public List<ExprNode> index() {
        return index;
    }

    public String ident() {
        return ident;
    }

    @Override
    public String toString() {
        return "LValNode{\n" +
                "ident='" + ident + '\'' +
                ", \nline=" + line +
                ",\n index=" + index +
                "\n}";
    }

    @Override
    public ExprNode simplify(SymbolTable symbolTable) {
        List<ExprNode> simplifiedIndex = new ArrayList<>();
        for (ExprNode exprNode : index) {
            simplifiedIndex.add(exprNode.simplify(symbolTable));
        }
        TableEntry tableEntry = symbolTable.getSymbolDefined(this.ident);
        if (tableEntry != null && tableEntry.isConst && this.dimension == 0) {
            for (ExprNode exprNode : simplifiedIndex) {
                if (!(exprNode instanceof NumberNode)) {
                    return new LValNode(dimension, valueType, ident, line, simplifiedIndex);
                }
            }
            for (ExprNode exprNode : tableEntry.dimension) {
                if (!(exprNode instanceof NumberNode)) {
                    return new LValNode(dimension, valueType, ident, line, simplifiedIndex);
                }
            }
            if (tableEntry.refType == TableEntry.RefType.ITEM) {
                if (tableEntry.initValue instanceof NumberNode) {
                    return new NumberNode(((NumberNode) tableEntry.initValue).number());
                } else {
                    return new LValNode(dimension, valueType, ident, line, simplifiedIndex);
                }
            } else if (tableEntry.refType == TableEntry.RefType.ARRAY) {
                int position = 0;
                for (int i = 0; i < simplifiedIndex.size(); i++) {
                    int temp = ((NumberNode) simplifiedIndex.get(i)).number();
                    for (int j = i + 1; j < tableEntry.dimension.size();
                         j++) {
                        temp = temp * ((NumberNode) tableEntry.dimension.get(j)).number();
                    }
                    position += temp;
                }
                if (tableEntry.initValueList.get(position) instanceof NumberNode) {
                    return new NumberNode(((NumberNode) tableEntry.initValueList.get(position)).number());
                } else {
                    return new LValNode(dimension, valueType, ident, line, simplifiedIndex);
                }
            } else {
                return new LValNode(dimension, valueType, ident, line, simplifiedIndex);
            }

        } else {
            return new LValNode(dimension, valueType, ident, line, simplifiedIndex);
        }
    }
}
