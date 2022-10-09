package front.nodes;

import front.CompileUnit;

import java.util.List;

public class FuncDefNode {
    public CompileUnit.Type type;
    public String name;
    public int line;
    public List<FuncParamNode> funcParams;
    public BlockNode blockNode;

    public FuncDefNode(CompileUnit.Type type, String name, int line, List<FuncParamNode> funcParams, BlockNode blockNode) {
        this.type = type;
        this.name = name;
        this.line = line;
        this.funcParams = funcParams;
        this.blockNode = blockNode;
    }

}
