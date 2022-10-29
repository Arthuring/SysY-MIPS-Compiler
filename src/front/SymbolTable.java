package front;

import exception.CompileExc;
import front.nodes.DefNode;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static final SymbolTable globalTable = new SymbolTable(null, "GLOBAL");
    private final SymbolTable parentTable;
    private final String filedName;
    private final Map<String, TableEntry> varSymbols = new HashMap<>();


    public SymbolTable(SymbolTable parentTable, String filedName) {
        this.parentTable = parentTable;
        this.filedName = filedName;
    }

    public SymbolTable getParentTable() {
        return parentTable;
    }

    public boolean isGlobalTable() {
        return this.parentTable == null && this.filedName.equals("GLOBAL");
    }

    public void addVarSymbol(DefNode defNode, int level, boolean isConst, CompileUnit.Type valueType)
            throws CompileExc {
        if (this.varSymbols.containsKey(defNode.ident())) {
            throw new CompileExc(CompileExc.ErrType.REDEF, defNode.line());
        } else {
            TableEntry tableEntry = new TableEntry(defNode, isConst, level, valueType, isGlobalTable());
            //TODO: GLOBAL初始化为0
            varSymbols.put(defNode.ident(), tableEntry);

        }
    }

    public void addVarSymbol(TableEntry tableEntry) {
        varSymbols.put(tableEntry.name, tableEntry);
    }

    public TableEntry getSymbol(String name) {
        if (varSymbols.containsKey(name)) {
            return varSymbols.get(name);
        } else if (this.parentTable != null) {
            return parentTable.getSymbol(name);
        } else {
            return null;
        }
    }

    public String filedName() {
        return filedName;
    }

    public static SymbolTable globalTable() {
        return globalTable;
    }
}
