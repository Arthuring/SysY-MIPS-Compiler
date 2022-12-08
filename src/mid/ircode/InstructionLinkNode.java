package mid.ircode;

import front.TableEntry;

import java.util.HashSet;
import java.util.Set;

public class InstructionLinkNode extends User {
    private InstructionLinkNode prev = null;
    private InstructionLinkNode next = null;
    private final int instrId;
    private static int ID_COUNTER = 0;

    public InstructionLinkNode() {
        this.instrId = ID_COUNTER;
        ID_COUNTER += 1;
    }

    public int getId() {
        return instrId;
    }

    public InstructionLinkNode next() {
        return next;
    }

    public InstructionLinkNode prev() {
        return prev;
    }

    public void setNext(InstructionLinkNode next) {
        this.next = next;
    }

    public void setPrev(InstructionLinkNode prev) {
        this.prev = prev;
    }

    public boolean hasPrev() {
        return this.prev == null;
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public void remove() {
        if (hasPrev()) {
            this.prev.setNext(this.next);
        }

        if (hasNext()) {
            this.next.setPrev(this.prev);
        }
    }

    public void insertBefore(InstructionLinkNode irLinkNode) {
        irLinkNode.setNext(this);
        irLinkNode.setPrev(this.prev);
        if (hasPrev()) {
            this.prev.setNext(irLinkNode);
        }
        this.prev = irLinkNode;
    }

    public void insertAfter(InstructionLinkNode irLinkNode) {
        irLinkNode.setPrev(this);
        irLinkNode.setNext(this.next);
        if (hasNext()) {
            this.next.setPrev(irLinkNode);
        }
        this.next = irLinkNode;
    }

    public void append(InstructionLinkNode irLinkNode) {
        InstructionLinkNode ptr = this;
        while (ptr.hasNext()) {
            ptr = ptr.next();
        }
        ptr.setNext(irLinkNode);
        irLinkNode.setPrev(ptr);
    }

    public Set<TableEntry> getUseVar() {
        return new HashSet<>();
    }

    public TableEntry getDefineVar() {
        return null;
    }

    public String toIr() {
        return null;
    }
}
