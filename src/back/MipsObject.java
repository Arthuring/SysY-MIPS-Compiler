package back;

import back.hardware.Memory;
import back.hardware.RF;
import back.instr.MipsInstr;
import mid.StringCounter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MipsObject {
    private final MipsInstr entry = new MipsInstr();
    private final MipsInstr exit = new MipsInstr();
    public static final int STR_START_ADDR = 0x10000000;
    public static final int DATA_START_ADDR = RF.GP_INIT;
    private final Memory initMem = new Memory();
    private String labelToSet = null;
    private final List<String> irDescription = new ArrayList<>();
    private String comment = null;


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
        if (!irDescription.isEmpty()) {
            after.setIrDescription(irDescription);
            irDescription.clear();
        }
        if (comment != null) {
            after.setComment(comment);
            comment = null;
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

    public void addIrDescription(String irDescription) {
        if (irDescription != null) {
            this.irDescription.add(irDescription);
        }
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(".data 0x" + Integer.toHexString(STR_START_ADDR));
        for (Map.Entry<String, String> stringStringEntry : StringCounter.getStr2Label().entrySet()) {
            sj.add(stringStringEntry.getValue() + ":" + " .asciiz " +
                    "\"" + stringStringEntry.getKey() + "\"");
        }
        sj.add(".data 0x" + Integer.toHexString(DATA_START_ADDR));
        for (int i = 0; i < initMem.getGlobalOffset(); i += 4) {
            sj.add(".word " + initMem.loadWord(i));
        }

        sj.add(".text");
        MipsInstr ptr = getFirstInstr();
        while (ptr.hasNext()) {
            sj.add(ptr.toMips());
            ptr = (MipsInstr) ptr.next();
        }
        return sj.toString();
    }
}
