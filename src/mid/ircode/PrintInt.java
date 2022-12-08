package mid.ircode;

import front.TableEntry;

import java.util.HashSet;
import java.util.Set;

public class PrintInt extends InstructionLinkNode {
    private final Operand value;

    public PrintInt(Operand value) {
        super();
        this.value = value;
    }

    public Operand getValue() {
        return value;
    }

    @Override
    public String toIr() {
        return "\t" + "call void @putint(" +
                ((value instanceof Immediate) ? "i32" :
                        TableEntry.TO_IR.get(((TableEntry) value).valueType))
                + " " + value.toNameIr() + " )";
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        if (value instanceof TableEntry) {
            useSet.add((TableEntry) value);
        }
        return useSet;
    }

    @Override
    public TableEntry getDefineVar() {
        return super.getDefineVar();
    }
}
