package mid.ircode;

import java.util.HashSet;
import java.util.StringJoiner;

public class BasicBlock extends InstructionLinkNode {
    private static int ID_COUNTER = 0;
    private final int id;
    private final String beginLabel;
    private final InstructionLinkNode end = new InstructionLinkNode();
    private String endLabel;
    private final HashSet<String> prevBlock = new HashSet<>();
    private final HashSet<String> nextBlock = new HashSet<>();

    public BasicBlock(String label) {
        this.id = ID_COUNTER;
        ID_COUNTER += 1;
        this.beginLabel = label;
        this.setNext(this.end);
        this.end.setPrev(this);
        this.endLabel = null;
    }

    public String getLabel() {
        return beginLabel;
    }

    public InstructionLinkNode getFirstInstruction() {
        return this.next();
    }

    public InstructionLinkNode getLastInstruction() {
        return end.prev();
    }

    public void addAfter(InstructionLinkNode instruct) {
        InstructionLinkNode last = getLastInstruction();
        last.setNext(instruct);
        instruct.setPrev(last);
        InstructionLinkNode ptr = instruct;
        while (ptr.hasNext()) {
            ptr = ptr.next();
        }
        end.setPrev(ptr);
        ptr.setNext(end);
    }

    public String toIr() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(beginLabel + ":");
        InstructionLinkNode ptr = this.next();
        while (ptr != this.end) {
            sj.add(ptr.toIr());
            ptr = ptr.next();
        }
        if (endLabel != null) {
            sj.add(endLabel + ":");
        }
        return sj.toString();
    }

    public InstructionLinkNode getEnd() {
        return end;
    }

    public String getEndLabel() {
        return endLabel;
    }

    public String getBeginLabel() {
        return beginLabel;
    }

    public void setEndLabel(String endLabel) {
        this.endLabel = endLabel;
    }

    public int getId() {
        return id;
    }

    public HashSet<String> getNextBlock() {
        return nextBlock;
    }

    public HashSet<String> getPrevBlock() {
        return prevBlock;
    }

    public void addPrevBlock(String id) {
        this.prevBlock.add(id);
    }

    public void addNextBlock(String id) {
        this.nextBlock.add(id);
    }

    public boolean isEmpty() {
        return getFirstInstruction() == end;
    }

    public void removeInstr(InstructionLinkNode instr) {
        instr.prev().setNext(instr.next());
        instr.next().setPrev(instr.prev());
    }
}
