package front.nodes;

import java.util.List;

public class CompileUnitNode implements SyntaxNode {
    private final List<DeclNode> declNodes;
    private final List<FuncDefNode> funcDefNodes;
    private final FuncDefNode mainFuncDef;

    public CompileUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, FuncDefNode mainFuncDef) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDef = mainFuncDef;
    }

    public List<DeclNode> declNodes() {
        return declNodes;
    }

    public List<FuncDefNode> funcDefNodes() {
        return funcDefNodes;
    }

    public FuncDefNode mainFuncDef() {
        return mainFuncDef;
    }

    @Override
    public String toString() {
        return "CompileUnitNode{\n" +
                "declNodes=" + declNodes +
                ",\n funcDefNodes=" + funcDefNodes +
                ",\n mainFuncDef=" + mainFuncDef +
                "\n}";
    }
}
