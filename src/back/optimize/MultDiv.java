package back.optimize;

import back.hardware.RF;
import back.instr.*;
import util.Pair;

public class MultDiv {


    public static MipsInstr betterMult(int dst, int n, int d) {
        int absd = d < 0 ? -d : d;
        if (absd == 1) {
            MipsInstr instr = new Move(dst, n);
            if (d < 0) {
                instr.append(new Subu(dst, RF.GPR.ZERO, dst));
            }
            return instr;
        } else if (isTimesTwo(absd)) {
            int l = timesTwo(absd);
            MipsInstr inst = new Sll(dst, n, l);
            if (d < 0) {
                inst.append(new Subu(dst, RF.GPR.ZERO, dst));
            }
            return inst;
        } else if (isTimesTwo(absd + 1)) {
            int l = timesTwo(absd + 1);
            MipsInstr inst = new Sll(dst, n, l);
            inst.append(new Subu(dst, dst, n));
            if (d < 0) {
                inst.append(new Subu(dst, RF.GPR.ZERO, dst));
            }
            return inst;
        } else if (isTimesTwo(absd - 1)) {
            int l = timesTwo(absd - 1);
            MipsInstr inst = new Sll(dst, n, l);
            inst.append(new Addu(dst, dst, n));
            if (d < 0) {
                inst.append(new Subu(dst, RF.GPR.ZERO, dst));
            }
            return inst;
        }
        MipsInstr noOpti = new Li(dst, d);
        noOpti.append(new Mul(dst, n, dst));
        return noOpti;
    }

    public static MipsInstr betterDiv(int dst, int n, int d) {
        int absd = d < 0 ? -d : d;
        if (absd == 1) {
            MipsInstr instr = new Move(dst, n);
            if (d < 0) {
                instr.append(new Subu(dst, RF.GPR.ZERO, dst));
            }
            return instr;
        } else if (isTimesTwo(absd)) {
            int l = timesTwo(absd);
            MipsInstr instr = new Sra(RF.GPR.V1, n, l - 1);
            instr.append(new Srl(RF.GPR.V1, RF.GPR.V1, 32 - l));
            instr.append(new Addu(RF.GPR.V1, RF.GPR.V1, n));
            instr.append(new Sra(dst, RF.GPR.V1, l));
            if (d < 0) {
                instr.append(new Subu(dst, RF.GPR.ZERO, dst));
            }
            return instr;
        } else {
            Pair<Long, Integer> ms = chooseMultiplier(absd, 31);
            long m = ms.o1;
            int shPost = ms.o2;
            if (m < ((long) 1 << 31)) {
                MipsInstr instr = new Li(dst, (int) m);
                instr.append(new Mult(dst, n));
                instr.append(new Mfhi(dst));
                instr.append(new Sra(dst, dst, shPost));
                instr.append(new Sra(RF.GPR.V1, n, 31));
                instr.append(new Subu(dst, dst, RF.GPR.V1));
                if (d < 0) {
                    instr.append(new Subu(dst, RF.GPR.ZERO, dst));
                }
                return instr;
            } else {
                MipsInstr instr = new Li(dst, (int) (m - ((long) 1 << 32)));
                instr.append(new Mult(dst, n));
                instr.append(new Mfhi(dst));
                instr.append(new Addu(dst, dst, n));
                instr.append(new Sra(dst, dst, shPost));
                instr.append(new Sra(RF.GPR.V1, n, 31));
                instr.append(new Subu(dst, dst, RF.GPR.V1));
                if (d < 0) {
                    instr.append(new Subu(dst, RF.GPR.ZERO, dst));
                }
                return instr;
            }
        }
    }

    public static MipsInstr betterMod(int dst, int n, int d) {
        MipsInstr instr = betterDiv(dst, n, d);
        instr.append(new Move(RF.GPR.V1, dst));
        instr.append(betterMult(dst, RF.GPR.V1, d));
        instr.append(new Subu(dst, n, dst));
        return instr;
    }

    public static boolean isTimesTwo(int n) {
        String str = Integer.toBinaryString(n);
        if (n < 1) {
            return false;
        } else {
            return str.lastIndexOf("1") == 0;
        }
    }

    public static int timesTwo(int n) {
        return Integer.toBinaryString(n).length() - 1;
    }

    public static Pair<Long, Integer> chooseMultiplier(int d, int prec) {
        int l = isTimesTwo(d) ? timesTwo(d) : timesTwo(d) + 1;
        int shPost = l;
        long mLow = ((long) 1 << (32 + l)) / d;
        long mHigh = (((long) 1 << (32 + l)) + ((long) 1 << (32 + l - prec))) / d;

        while ((mLow / 2) < (mHigh / 2) && shPost > 0) {
            mLow = mLow / 2;
            mHigh = mHigh / 2;
            shPost = shPost - 1;
        }
        return new Pair<>(mHigh, shPost);
    }
}
