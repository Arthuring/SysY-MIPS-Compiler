package front;

import exception.CompileExc;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final SymbolTable parentTable;
    private final Map<String, TableEntry> varSymbols = new HashMap<>();
    private final Map<String, TableEntry> funcSymbols = new HashMap<>();

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
    }

    public SymbolTable getParentTable() {
        return parentTable;
    }

    public boolean isGlobalTable() {
        return this.parentTable == null;
    }

    public void addVarSymbol(String name, TableEntry.ValueType valueType, TableEntry.SymbolType symbolType, int value, int level)
            throws CompileExc {
        if (this.varSymbols.containsKey(name)) {
            throw new CompileExc(CompileExc.ErrType.REDEF, name);
        } else {
            varSymbols.put(name, new TableEntry(symbolType, valueType, name, value, level));
        }
    }
}
