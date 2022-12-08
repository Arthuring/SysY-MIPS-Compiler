package mid.optimize;

import front.TableEntry;
import mid.ircode.*;

import java.util.*;

public class ReachingDefTable {
    private final Map<String, Set<InstructionLinkNode>> gen = new HashMap<>();
    private final Map<String, Set<InstructionLinkNode>> kill = new HashMap<>();
    private final Map<String, Set<InstructionLinkNode>> in = new HashMap<>();
    private final Map<String, Set<InstructionLinkNode>> out = new HashMap<>();

    private final Map<TableEntry, Set<InstructionLinkNode>> define = new HashMap<>();
    private boolean changed = false;

    public ReachingDefTable() {

    }

    public Map<String, Set<InstructionLinkNode>> getGen() {
        return gen;
    }

    public Map<String, Set<InstructionLinkNode>> getKill() {
        return kill;
    }

    public Map<String, Set<InstructionLinkNode>> getIn() {
        return in;
    }

    public Map<String, Set<InstructionLinkNode>> getOut() {
        return out;
    }

    public boolean isChanged() {
        return changed;
    }

    public Map<TableEntry, Set<InstructionLinkNode>> getDefine() {
        return define;
    }

    public void signDef(InstructionLinkNode instr) {
        if (instr instanceof BinaryOperator) {
            TableEntry dst = ((BinaryOperator) instr).getDst();
            if (!define.containsKey(dst)) {
                define.put(dst, new HashSet<>());
            }
            define.get(dst).add(instr);
        } else if (instr instanceof UnaryOperator) {
            TableEntry dst = ((UnaryOperator) instr).getDst();
            if (!define.containsKey(dst)) {
                define.put(dst, new HashSet<>());
            }
            define.get(dst).add(instr);
        } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.STORE
                && ((PointerOp) instr).getDst().refType != TableEntry.RefType.POINTER) {
            TableEntry dst = ((PointerOp) instr).getDst();
            if (!define.containsKey(dst)) {
                define.put(dst, new HashSet<>());
            }
            define.get(dst).add(instr);
        } else if (instr instanceof Input) {
            TableEntry dst = ((Input) instr).getDst();
            if (!define.containsKey(dst)) {
                define.put(dst, new HashSet<>());
            }
            define.get(dst).add(instr);
        } else if (instr instanceof Call && ((Call) instr).getReturnDst() != null) {
            TableEntry dst = ((Call) instr).getReturnDst();
            if (!define.containsKey(dst)) {
                define.put(dst, new HashSet<>());
            }
            define.get(dst).add(instr);
        } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.LOAD) {
            TableEntry dst = ((PointerOp) instr).getDst();
            if (!define.containsKey(dst)) {
                define.put(dst, new HashSet<>());
            }
            define.get(dst).add(instr);
        }
    }

    public void calculateGenKill(BasicBlock basicBlock) {
        Set<InstructionLinkNode> genBlock = new HashSet<>();
        Set<InstructionLinkNode> killBlock = new HashSet<>();
        InstructionLinkNode ptr = basicBlock.getEnd().prev();
        while (ptr != basicBlock) {
            if (isDefine(ptr) && !getDst(ptr).isTemp) {
                genBlock = cup(genBlock, sub(ptr, killBlock));
                killBlock = cup(killBlock, getKillOfInstr(ptr));
            }
            ptr = ptr.prev();
        }
        gen.put(basicBlock.getLabel(), genBlock);
        kill.put(basicBlock.getLabel(), killBlock);
        in.put(basicBlock.getLabel(), new HashSet<>());
        out.put(basicBlock.getLabel(), new HashSet<>());
    }

    public void calculateInOut(List<BasicBlock> basicBlocks) {
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlocks) {
                Set<InstructionLinkNode> inBlock = new HashSet<>();

                for (String pre : basicBlock.getPrevBlock()) {
                    inBlock = cup(inBlock, out.getOrDefault(pre, new HashSet<>()));
                }
                if (!changed && !in.get(basicBlock.getLabel()).equals(inBlock)) {
                    changed = true;
                }
                in.put(basicBlock.getLabel(), inBlock);

                Set<InstructionLinkNode> outBlock = cup(gen.get(basicBlock.getLabel()),
                        sub(in.get(basicBlock.getLabel()), kill.get(basicBlock.getLabel())));
                if (!changed && !out.get(basicBlock.getLabel()).equals(outBlock)) {
                    changed = true;
                }
                out.put(basicBlock.getLabel(), outBlock);
            }
        } while (isChanged());
    }

    public boolean isDefine(InstructionLinkNode instr) {
        if (instr instanceof BinaryOperator) {
            return true;
        } else if (instr instanceof UnaryOperator) {
            return true;
        } else if (instr instanceof Input) {
            return true;
        } else if (instr instanceof Call && ((Call) instr).getReturnDst() != null) {
            return true;
        } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.LOAD) {
            return true;
        } else {
            return instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.STORE
                    && ((PointerOp) instr).getDst().refType != TableEntry.RefType.POINTER;
        }
    }

    public Set<InstructionLinkNode> cup(Set<InstructionLinkNode> a, Set<InstructionLinkNode> b) {
        HashSet<InstructionLinkNode> ans = new HashSet<>();
        ans.addAll(a);
        ans.addAll(b);
        return ans;
    }

    public Set<InstructionLinkNode> cup(Set<InstructionLinkNode> b, InstructionLinkNode a) {
        HashSet<InstructionLinkNode> ans = new HashSet<>();
        ans.add(a);
        ans.addAll(b);
        return ans;
    }

    public Set<InstructionLinkNode> getKillOfInstr(InstructionLinkNode instr) {
        HashSet<InstructionLinkNode> ans = new HashSet<>(define.getOrDefault(getDst(instr), new HashSet<>()));
        ans.remove(instr);
        return ans;
    }

    public Set<InstructionLinkNode> sub(InstructionLinkNode a, Set<InstructionLinkNode> b) {
        Set<InstructionLinkNode> ans = new HashSet<>();
        ans.add(a);
        ans.removeAll(b);
        return ans;
    }

    public Set<InstructionLinkNode> sub(Set<InstructionLinkNode> a, Set<InstructionLinkNode> b) {
        Set<InstructionLinkNode> ans = new HashSet<>(a);
        ans.removeAll(b);
        return ans;
    }

    public TableEntry getDst(InstructionLinkNode instr) {
        if (instr instanceof BinaryOperator) {
            return ((BinaryOperator) instr).getDst();
        } else if (instr instanceof UnaryOperator) {
            return ((UnaryOperator) instr).getDst();
        } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.STORE
                && ((PointerOp) instr).getDst().refType != TableEntry.RefType.POINTER) {
            return ((PointerOp) instr).getDst();
        } else if (instr instanceof Input) {
            return ((Input) instr).getDst();
        } else if (instr instanceof Call) {
            return ((Call) instr).getReturnDst();
        } else if (instr instanceof PointerOp && ((PointerOp) instr).getOp() == PointerOp.Op.LOAD) {
            return ((PointerOp) instr).getDst();
        }
        return null;
    }

    public Set<InstructionLinkNode> cap(Set<InstructionLinkNode> a, Set<InstructionLinkNode> b) {
        HashSet<InstructionLinkNode> ans = new HashSet<>(a);
        ans.retainAll(b);
        return ans;
    }

    public Set<InstructionLinkNode> getReachDef(TableEntry tableEntry, String blockLabel) {
        return cap(define.getOrDefault(tableEntry, new HashSet<>()), in.getOrDefault(blockLabel, new HashSet<>()));
    }
}
