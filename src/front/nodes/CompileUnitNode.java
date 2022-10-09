package front.nodes;

import java.util.List;

public class CompileUnitNode implements SyntaxNode {
    public List<DeclNode> declNodes;
    public List<FuncDefNode> funcDefNodes;
    public FuncDefNode mainFuncDef;

    public CompileUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, FuncDefNode mainFuncDef) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDef = mainFuncDef;
    }
}
