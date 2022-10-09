package front;

public class TableEntry {
    public enum ValueType {
        INT,
        INT_ARR,
        VOID,
        INVALID
    }

    public enum SymbolType {
        CONST, VAR, FUNC, PARAM
    }

    public SymbolType symbolType;
    public ValueType valueType;
    public String name;
    public Integer value;
    public int dimension;
    public int dim0Size;
    public int dim1Size;
    public int level;
    public long addr;
    public int size;

    public TableEntry(SymbolType symbolType, ValueType valueType, String name, Integer value,
                      int dimension, int dim0Size, int dim1Size, int level, int addr, int size) {
        this.symbolType = symbolType;
        this.valueType = valueType;
        this.name = name;
        this.value = value;
        this.dimension = dimension;
        this.dim0Size = dim0Size;
        this.dim1Size = dim1Size;
        this.level = level;
        this.addr = addr;
        this.size = size;
    }

    public TableEntry(SymbolType symbolType, ValueType valueType, String name, Integer value, int level) {
        this.symbolType = symbolType;
        this.valueType = valueType;
        this.name = name;
        this.value = value;
        this.level = level;
    }
}
