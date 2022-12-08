package mid.ircode;

import front.TableEntry;

import java.util.*;

public class ElementPtr extends InstructionLinkNode {
    private final TableEntry dst;
    private final TableEntry.RefType refType;
    private final TableEntry.ValueType valueType;
    private final TableEntry baseVar;
    private final List<Operand> index = new ArrayList<>();

    public ElementPtr(TableEntry dst, TableEntry baseVar, List<Operand> index) {
        super();
        this.dst = dst;
        this.baseVar = baseVar;
        this.refType = baseVar.refType;
        this.valueType = baseVar.valueType;
        this.index.addAll(index);
    }

    @Override
    public String toIr() {
        StringJoiner sj = new StringJoiner(", ");
        for (Operand operand : index) {
            if (operand instanceof Immediate) {
                sj.add("i32 " + ((Immediate) operand).getValue());
            } else {
                sj.add("i32 " + operand.toNameIr());
            }
        }
        return "\t" + dst.toNameIr() + " = getelementptr " + baseVar.typeToIr() + ", "
                + baseVar.typeToIr() + "* "
                + baseVar.toNameIr() + ", " + sj;
    }

    public TableEntry getDst() {
        return dst;
    }

    public TableEntry getBaseVar() {
        return baseVar;
    }

    public List<Operand> getIndex() {
        return index;
    }

    @Override
    public TableEntry getDefineVar() {
        return dst;
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        for (Operand operand : index) {
            if (operand instanceof TableEntry) {
                useSet.add((TableEntry) operand);
            }
        }
        return useSet;
    }
}
