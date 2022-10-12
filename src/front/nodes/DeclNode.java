package front.nodes;

import front.CompileUnit;
import front.Parser;

import java.util.List;

public class DeclNode implements BlockItemNode {
    private final boolean isConst;
    private final CompileUnit.Type type;
    private final List<DefNode> defNodeList;

    public DeclNode(boolean isConst, CompileUnit.Type type, List<DefNode> defNodeList) {
        this.isConst = isConst;
        this.type = type;
        this.defNodeList = defNodeList;
    }

    public boolean isConst() {
        return isConst;
    }

    public List<DefNode> defNodeList() {
        return defNodeList;
    }

    public CompileUnit.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DeclNode{\n" +
                "isConst=" + isConst +
                ",\n type=" + type +
                ",\n defNodeList=" + defNodeList +
                "\n}";
    }
}
