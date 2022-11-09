package mid.ircode;

import front.TableEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ElementPtr extends InstructionLinkNode {
    private final TableEntry dst;
    private final TableEntry.RefType refType;
    private final TableEntry.ValueType valueType;
    private final TableEntry baseVar;
    private final List<Operand> index = new ArrayList<>();

    public ElementPtr(TableEntry dst, TableEntry baseVar, List<Operand> index) {
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
}
