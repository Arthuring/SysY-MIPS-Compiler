package mid.optimize;

import front.TableEntry;
import mid.ircode.InstructionLinkNode;

import java.util.HashSet;
import java.util.Set;


public class Util {
    public static <T> Set<T> cap(Set<T> a, Set<T> b) {
        HashSet<T> ans = new HashSet<>(a);
        ans.retainAll(b);
        return ans;
    }

    public static <T> Set<T> sub(T a, Set<T> b) {
        Set<T> ans = new HashSet<>();
        ans.add(a);
        ans.removeAll(b);
        return ans;
    }

    public static <T> Set<T> sub(Set<T> a, Set<T> b) {
        Set<T> ans = new HashSet<>(a);
        ans.removeAll(b);
        return ans;
    }

    public static <T> Set<T> sub(Set<T> a, T b) {
        Set<T> ans = new HashSet<>(a);
        ans.remove(b);
        return ans;
    }

    public static <T> Set<T> cup(Set<T> a, Set<T> b) {
        Set<T> newSet = new HashSet<>(a);
        newSet.addAll(b);
        return newSet;
    }

    public static <T> Set<T> cup(Set<T> b, T a) {
        HashSet<T> ans = new HashSet<>();
        ans.add(a);
        ans.addAll(b);
        return ans;
    }

    public static void replaceInstr(InstructionLinkNode oldInstr, InstructionLinkNode newInstr) {
        newInstr.setPrev(oldInstr.prev());
        newInstr.setNext(oldInstr.next());
        oldInstr.prev().setNext(newInstr);
        oldInstr.next().setPrev(newInstr);
    }

    public static void removeInstr(InstructionLinkNode instr) {
        instr.prev().setNext(instr.next());
        instr.next().setPrev(instr.prev());
    }

}
