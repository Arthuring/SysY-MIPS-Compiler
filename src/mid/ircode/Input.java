package mid.ircode;

import front.TableEntry;

public class Input extends InstructionLinkNode {
    private final TableEntry dst;

    public Input(TableEntry dst) {
        this.dst = dst;
    }

    public TableEntry getDst() {
        return dst;
    }

    @Override
    public String toIr() {
        return "\t" + dst.toNameIr() + " = call i32 @getint()";
    }
}
