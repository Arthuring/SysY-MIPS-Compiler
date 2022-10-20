package exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CompileExc extends Exception implements Comparable<CompileExc> {
    public enum ErrCode {
        a, b, c, d, e, f, g, h, i, j, k, l, m
    }

    public enum ErrType {
        ILLEGAL_CHAR,
        REDEF,
        UNDECL,
        ARG_NUM_MISMATCH,
        ARG_TYPE_MISMATCH,
        RET_TYPE_MISMATCH,
        MISSING_RET,
        CHANGE_CONST,
        EXPECTED_SEMICN,
        EXPECTED_PARENT,
        EXPECTED_BRACK,
        FORMAT_VAR_ERR,
        NOT_IN_LOOP,

    }

    private static final Map<ErrType, ErrCode> TYPE_2_CODE = new HashMap<ErrType, ErrCode>() {
        {
            put(ErrType.ILLEGAL_CHAR, ErrCode.a);
            put(ErrType.REDEF, ErrCode.b);
            put(ErrType.UNDECL, ErrCode.c);
            put(ErrType.ARG_NUM_MISMATCH, ErrCode.d);
            put(ErrType.ARG_TYPE_MISMATCH, ErrCode.e);
            put(ErrType.RET_TYPE_MISMATCH, ErrCode.f);
            put(ErrType.MISSING_RET, ErrCode.g);
            put(ErrType.CHANGE_CONST, ErrCode.h);
            put(ErrType.EXPECTED_SEMICN, ErrCode.i);
            put(ErrType.EXPECTED_PARENT, ErrCode.j);
            put(ErrType.EXPECTED_BRACK, ErrCode.k);
            put(ErrType.FORMAT_VAR_ERR, ErrCode.l);
            put(ErrType.NOT_IN_LOOP, ErrCode.m);
        }
    };

    private final ErrType errType;
    private int lineNo = 0;
    private final String msg;

    public CompileExc(ErrType errType, int lineNo, String msg) {
        this.errType = errType;
        this.lineNo = lineNo;
        this.msg = msg;
    }

    public CompileExc(ErrType errType, int lineNo) {
        this.errType = errType;
        this.lineNo = lineNo;
        this.msg = "";
    }

    public CompileExc(ErrType errType, String msg) {
        this.errType = errType;
        this.msg = msg;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String toString() {
        return this.lineNo + " " + TYPE_2_CODE.get(this.errType).toString();
    }

    @Override
    public int compareTo(CompileExc o) {
        return this.lineNo - o.lineNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompileExc that = (CompileExc) o;
        return lineNo == that.lineNo && errType == that.errType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errType, lineNo);
    }
}
