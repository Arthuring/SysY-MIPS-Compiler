package mid.ircode;

import front.TableEntry;

import java.util.HashSet;
import java.util.Set;

public class PointerOp extends InstructionLinkNode {
    public enum Op {
        LOAD, STORE
    }

    private final Op op;
    private final Operand src;
    private final TableEntry dst;

    public PointerOp(Op op, TableEntry dst, Operand src) {
        super();
        this.op = op;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toIr() {
        if (this.op == Op.LOAD) {
            return "\t" + dst.toNameIr() + " = load " +
                    dst.typeToIr() + ", " +
                    src.typeToIr() + "* " + src.toNameIr();
        } else {
            return "\t" + "store " + dst.typeToIr() + " " + src.toNameIr() + ", "
                    + dst.typeToIr() + "* " + dst.toNameIr();
        }
    }

    public TableEntry getDst() {
        return dst;
    }

    public Operand getSrc() {
        return src;
    }

    public Op getOp() {
        return op;
    }

    @Override
    public TableEntry getDefineVar() {
        if (op == Op.LOAD) {
            return dst;
        } else {
            if (dst.refType == TableEntry.RefType.ITEM) {
                return dst;
            }
        }
        return null;
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        if (src instanceof TableEntry) {
            useSet.add((TableEntry) src);
        }
        if (op == Op.STORE && dst.refType == TableEntry.RefType.POINTER) {
            useSet.add(dst);
        }
        return useSet;
    }
}
