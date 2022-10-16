package front;

import exception.CompileExc;
import front.nodes.FuncParamNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuncEntry {

    private final String name;
    private final List<TableEntry> args = new ArrayList<>();
    private final Map<String, TableEntry> name2entry = new HashMap<>();
    private final boolean isMain;
    private final TableEntry.ValueType returnType;

    public TableEntry.ValueType returnType() {
        return returnType;
    }

    public FuncEntry(String name, TableEntry.ValueType returnType) {
        this.name = name;
        this.returnType = returnType;
        this.isMain = false;
    }

    public String name() {
        return name;
    }

    public List<TableEntry> args() {
        return args;
    }

    public Map<String, TableEntry> name2entry() {
        return name2entry;
    }

    public void addArg(FuncParamNode funcParamNode) throws CompileExc {
        if (name2entry.containsKey(funcParamNode.ident())) {
            throw new CompileExc(CompileExc.ErrType.REDEF, funcParamNode.line());
        } else {
            TableEntry tableEntry = new TableEntry(funcParamNode);
            name2entry.put(funcParamNode.ident(), tableEntry);
            args.add(tableEntry);
        }
    }

    public FuncEntry() {
        this.name = "main";
        this.returnType = TableEntry.ValueType.INT;
        this.isMain = true;
    }

    public FuncEntry(String name, CompileUnit.Type type) {
        this.name = name;
        this.returnType = TableEntry.TO_VALUE_TYPE.get(type);
        this.isMain = name.equals("main");
    }
}
