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

}
