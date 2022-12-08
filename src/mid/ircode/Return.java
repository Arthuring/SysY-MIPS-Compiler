package mid.ircode;

import front.TableEntry;

import java.util.HashSet;
import java.util.Set;

public class Return extends InstructionLinkNode {
    private final Operand returnValue;

    public Return(Operand operand) {
        super();
        this.returnValue = operand;
    }

    public Operand getReturnValue() {
        return returnValue;
    }

    @Override
    public String toIr() {
        if (returnValue != null) {
            return "\tret " + ((returnValue instanceof Immediate) ? "i32" :
                    TableEntry.TO_IR.get(((TableEntry) returnValue).valueType))
                    + " " + returnValue.toNameIr();
        } else {
            return "\t ret void";
        }
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        if (returnValue != null && returnValue instanceof TableEntry) {
            useSet.add((TableEntry) returnValue);
        }
        return useSet;
    }

    @Override
    public TableEntry getDefineVar() {
        return super.getDefineVar();
    }
}
