package mid.ircode;

import front.TableEntry;

public class VarDef extends InstructionLinkNode {
    private final TableEntry tableEntry;

    public VarDef(TableEntry tableEntry) {
        this.tableEntry = tableEntry;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    @Override
    public String toIr() {
        return "\t" + tableEntry.toNameIr() + " = alloca " + TableEntry.TO_IR.get(tableEntry.valueType);
    }
}
