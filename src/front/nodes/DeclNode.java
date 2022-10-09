package front.nodes;

import front.CompileUnit;
import front.Parser;

import java.util.List;

public class DeclNode implements BlockItemNode {
    public final boolean isConst;
    public final List<DefNode> defNodeList;

    public DeclNode(boolean isConst, List<DefNode> defNodeList) {
        this.isConst = isConst;
        this.defNodeList = defNodeList;
    }
}
