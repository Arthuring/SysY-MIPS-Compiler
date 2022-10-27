package mid.ircode;

public class BasicBlock extends InstructionLinkNode {
    private final String label;
    private final InstructionLinkNode end = new InstructionLinkNode();

    public BasicBlock(String label) {
        this.label = label;
        this.setNext(this.end);
        this.end.setPrev(this);
    }

    public String getLabel() {
        return label;
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
}
