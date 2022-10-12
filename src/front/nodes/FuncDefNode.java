package front.nodes;

import front.CompileUnit;

import java.util.List;

public class FuncDefNode {
    private final CompileUnit.Type type;
    private final String name;
    private final int line;
    private final List<FuncParamNode> funcParams;
    private final BlockNode blockNode;

    public CompileUnit.Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public List<FuncParamNode> funcParams() {
        return funcParams;
    }

    public BlockNode blockNode() {
        return blockNode;
    }

    public int line() {
        return line;
    }

    public FuncDefNode(CompileUnit.Type type, String name, int line, List<FuncParamNode> funcParams, BlockNode blockNode) {
        this.type = type;
        this.name = name;
        this.line = line;
        this.funcParams = funcParams;
        this.blockNode = blockNode;
    }

    @Override
    public String toString() {
        return "FuncDefNode{\n" +
                "type=" + type +
                ",\n name='" + name + '\'' +
                ",\n line=" + line +
                ",\n funcParams=" + funcParams +
                ",\n blockNode=" + blockNode +
                "\n}";
    }
}
