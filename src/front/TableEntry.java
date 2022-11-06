package front;

import front.nodes.DefNode;
import front.nodes.ExprNode;
import front.nodes.FuncParamNode;
import front.nodes.NumberNode;
import mid.ircode.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TableEntry implements Operand {
    public enum ValueType {
        INT(4),
        VOID(4),
        ;
        private final int size;

        ValueType(int size) {
            this.size = size;
        }

        public int sizeof() {
            return size;
        }
    }

    public static final Map<ValueType, String> TO_IR = new HashMap<ValueType, String>() {
        {
            put(ValueType.INT, "i32");
            put(ValueType.VOID, "void");
        }
    };

    public enum RefType {
        ITEM, ARRAY, POINTER
    }

    public static final Map<CompileUnit.Type, ValueType> TO_VALUE_TYPE = new HashMap<CompileUnit.Type, ValueType>() {
        {
            put(CompileUnit.Type.INTTK, ValueType.INT);
            put(CompileUnit.Type.VOIDTK, ValueType.VOID);
        }
    };

    public final RefType refType;
    public final ValueType valueType;
    public final String name;
    public ExprNode initValue = null;
    public List<ExprNode> initValueList = null;
    public List<ExprNode> dimension;
    public final int level;
    public final boolean isConst;
    public final boolean isGlobal;
    public int address = 0;
    public boolean isTemp = false;
    public boolean defined = false;

    public TableEntry(RefType symbolType, ValueType valueType, String name, Integer initValue, boolean isConst,
                      int level, boolean isGlobal) {
        this.refType = symbolType;
        this.valueType = valueType;
        this.name = name;
        this.initValue = new NumberNode(initValue);
        this.isConst = isConst;
        this.level = level;
        this.isGlobal = isGlobal;
    }

    public TableEntry(RefType symbolType, ValueType valueType, String name, Integer initValue, boolean isConst,
                      int level, boolean isGlobal, boolean isTemp) {
        this.refType = symbolType;
        this.valueType = valueType;
        this.name = name;
        this.initValue = new NumberNode(initValue);
        this.isConst = isConst;
        this.level = level;
        this.isGlobal = isGlobal;
        this.isTemp = isTemp;
    }

    public TableEntry(DefNode defNode, boolean isConst, int level, CompileUnit.Type type, boolean isGlobal) {
        this.isConst = isConst;
        this.dimension = defNode.dimension();
        this.name = defNode.ident();
        if (defNode.dimension().size() == 0) {
            this.refType = RefType.ITEM;
            if (defNode.initValues().size() > 0) {
                this.initValue = defNode.initValues().get(0);
            }
        } else {
            this.refType = RefType.ARRAY;
            this.initValueList = defNode.initValues();
        }
        this.level = level;
        this.valueType = TO_VALUE_TYPE.get(type);
        this.isGlobal = isGlobal;
    }

    public TableEntry(FuncParamNode funcParamNode) {
        this.isConst = false;
        this.dimension = funcParamNode.dimension();
        this.name = funcParamNode.ident();
        this.level = 1;
        if (funcParamNode.dimension().size() == 0) {
            this.refType = RefType.ITEM;
        } else {
            this.refType = RefType.ARRAY;
        }
        this.valueType = TO_VALUE_TYPE.get(funcParamNode.type());
        this.isGlobal = false;
    }

    public void simplify(SymbolTable symbolTable) {
        if (initValue != null) {
            initValue = initValue.simplify(symbolTable);
        }
        if (initValueList != null) {
            List<ExprNode> newInitValueList = new ArrayList<>();
            for (ExprNode exprNode : initValueList) {
                newInitValueList.add(exprNode.simplify(symbolTable));
            }
            initValueList = newInitValueList;
        }
        List<ExprNode> newDimension = new ArrayList<>();
        for (ExprNode exprNode : dimension) {
            newDimension.add(exprNode.simplify(symbolTable));
        }
        dimension = newDimension;
    }

    public String toGlobalIr() {
        if (initValue != null) {
            return "@" + name + " = dso_local global "
                    + TO_IR.get(this.valueType) + " "
                    + ((NumberNode) initValue).number();
        } else {
            return "@" + name + " = dso_local global "
                    + TO_IR.get(this.valueType) + " "
                    + 0;
        }
    }

    public String toParamIr() {
        return TO_IR.get(valueType)
                + ((refType == RefType.ARRAY || refType == RefType.POINTER) ? "* " : " ")
                + toNameIr();
    }

    public String toNameIr() {
        if (!isGlobal) {
            return "%" + name + "_" + level;
        } else {
            return "@" + name;
        }
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableEntry that = (TableEntry) o;
        return level == that.level && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, level);
    }

    public int sizeof() {
        if (refType == RefType.ITEM) {
            return valueType.sizeof();
        } else {
            int temp = valueType.sizeof();
            for (ExprNode exprNode : dimension) {
                temp *= ((NumberNode) exprNode).number();
            }
            return temp;
        }
    }

    public boolean isTemp() {
        return isTemp;
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
    }

}
