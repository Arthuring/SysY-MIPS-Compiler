package mid.ircode;

import front.TableEntry;

import java.util.HashSet;
import java.util.Set;

public class Branch extends InstructionLinkNode {
    private final Operand cond;
    private String labelTrue;
    private String labelFalse;
    private final BrOp brOp;

    /**
     * BEQ: Jump when cond == 0, means jump when cond false, target: labelFalse
     * BNE: Jump when cond != 0, means jump when cond true, target: labelTrue
     */
    public enum BrOp {
        BEQ, BNE
    }

    public Branch(Operand cond, String labelTrue, String labelFalse, BrOp brOp) {
        super();
        this.cond = cond;
        this.labelFalse = labelFalse;
        this.labelTrue = labelTrue;
        this.brOp = brOp;
    }

    public Operand getCond() {
        return cond;
    }

    public String getLabelFalse() {
        return labelFalse;
    }

    public String getLabelTrue() {
        return labelTrue;
    }

    public BrOp getBrOp() {
        return brOp;
    }

    public String toIr() {
        return "\tbr " + cond.toNameIr() + " label %" + labelTrue + " label %" + labelFalse;
    }

    public void replaceTarget(String old, String newTarget) {
        if (labelTrue.equals(old)) {
            labelTrue = newTarget;
        }
        if (labelFalse.equals(old)) {
            labelFalse = newTarget;
        }
    }

    @Override
    public Set<TableEntry> getUseVar() {
        Set<TableEntry> useSet = new HashSet<>();
        if (cond instanceof TableEntry) {
            useSet.add((TableEntry) cond);
        }
        return useSet;
    }

    @Override
    public TableEntry getDefineVar() {
        return null;
    }
}
