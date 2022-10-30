package back;

import back.hardware.Memory;
import back.hardware.RF;
import back.instr.MipsInstr;

public class MipsObject {
    private final MipsInstr entry = new MipsInstr();
    private final MipsInstr exit = new MipsInstr();
    public static final int STR_START_ADDR = 0x10000000;
    public static final int DATA_START_ADDR = RF.GP_INIT;
    private final Memory initMem = new Memory();
    private String labelToSet = null;

    public MipsObject() {
        entry.setNext(exit);
        exit.setPrev(entry);
    }

    public MipsInstr getFirstInstr() {
        return (MipsInstr) entry.next();
    }

    public MipsInstr getLastInstr() {
        return (MipsInstr) exit.prev();
    }

    public void addAfter(MipsInstr after) {
        if (labelToSet != null) {
            after.setLabel(labelToSet);
            labelToSet = null;
        }
        MipsInstr lastInst = getLastInstr();
        lastInst.setNext(after);
        after.setPrev(lastInst);
        MipsInstr ptr = after;
        while (ptr.hasNext()) {
            ptr = (MipsInstr) ptr.next();
        }
        exit.setPrev(ptr);
        ptr.setNext(exit);
    }

    public Memory initMem() {
        return initMem;
    }

    public void setLabelToSet(String labelToSet) {
        this.labelToSet = labelToSet;
    }
}
