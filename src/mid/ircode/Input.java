package mid.ircode;

import front.TableEntry;

import java.util.HashSet;
import java.util.Set;

public class Input extends InstructionLinkNode {
    private final TableEntry dst;

    public Input(TableEntry dst) {
        super();
        this.dst = dst;
    }

    @Override
    public TableEntry getDefineVar() {
        return dst;
    }

    @Override
    public Set<TableEntry> getUseVar() {
        return new HashSet<>();
    }

    public TableEntry getDst() {
        return dst;
    }

    @Override
    public String toIr() {
        return "\t" + dst.toNameIr() + " = call i32 @getint()";
    }
}
