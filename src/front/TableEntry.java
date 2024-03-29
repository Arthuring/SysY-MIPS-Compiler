package front;

import front.nodes.DefNode;
import front.nodes.ExprNode;
import front.nodes.FuncParamNode;
import front.nodes.NumberNode;
import mid.ircode.Operand;

import java.util.*;

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

    public final RefType refType;//包括ITEM-普通变量,ARRAY-数组,POINTER-指针,三种类型
    public final ValueType valueType;//包括INT-整数,VOID-空，两种类型
    public final String name;//变量名
    public ExprNode initValue = null;//初始值
    public List<ExprNode> initValueList = null;//数组初始值
    public List<ExprNode> dimension;//数组每一维的大小
    public final int level;//定义处的层数
    public final boolean isConst;//是否是常量
    public final boolean isGlobal;//是否是全局变量
    public int address = 0;
    public boolean isTemp = false;
    public boolean defined = false;
    public boolean isParameter = false;//是否是函数参数;

    public List<ExprNode> getDimension() {
        return dimension;
    }

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

    //for tempItem
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

    //for tempPointer
    public TableEntry(RefType symbolType, ValueType valueType, String name,
                      List<ExprNode> dim) {
        this.refType = symbolType;
        this.valueType = valueType;
        this.name = name;
        this.isConst = false;
        this.level = 0;
        this.isGlobal = false;
        this.dimension = dim;
        this.isTemp = true;
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
            this.refType = RefType.POINTER;
        }
        this.valueType = TO_VALUE_TYPE.get(funcParamNode.type());
        this.isGlobal = false;
        this.isParameter = true;
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
        int space = 1;
        for (ExprNode exprNode : dimension) {
            space = space * ((NumberNode) exprNode).number();
        }

        if (refType == RefType.ARRAY && (initValueList == null || initValueList.size() != space)) {
            initValueList = new ArrayList<>();
            for (int i = 0; i < space; i++) {
                initValueList.add(new NumberNode(0));
            }
        }
    }

    public String toGlobalIr() {
        if (this.refType == RefType.ITEM) {
            if (initValue != null) {
                return "@" + name + " = dso_local global "
                        + TO_IR.get(this.valueType) + " "
                        + ((NumberNode) initValue).number();
            } else {
                return "@" + name + " = dso_local global "
                        + TO_IR.get(this.valueType) + " "
                        + 0;
            }
        } else {
            if (initValue != null) {
                StringBuilder sb = new StringBuilder("@" + name + " = dso_local global "
                        + ((isConst) ? " constant " : "")
                        + typeToIr() + " ");

                StringJoiner sji = new StringJoiner(", ");
                for (ExprNode exprNode : initValueList) {
                    sji.add("i32 " + ((NumberNode) exprNode).number());
                }
                sb.append("[").append(sji).append("]");

                return sb.toString();
            } else {
                return "@" + name + " = dso_local global "
                        + ((isConst) ? " constant " : "")
                        + typeToIr() + " zeroinitializer";
            }
        }
    }

    public String toParamIr() {
        return typeToIr() + " "
                + toNameIr();
    }

    public String toNameIr() {
        if (!isGlobal) {
            return "%" + name + "_" + level;
        } else {
            return "@" + name;
        }
    }

    public String typeToIr() {
        if (this.refType == RefType.ITEM) {
            return TO_IR.get(valueType);
        } else if (this.refType == RefType.ARRAY) {
            StringBuilder sb = new StringBuilder(TO_IR.get(valueType));
            for (int i = dimension.size() - 1; i >= 0; i--) {
                sb.append("]");
                sb.insert(0, "[" + ((NumberNode) dimension.get(i)).number() + " x ");
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder(TO_IR.get(valueType));
            for (int i = dimension.size() - 1; i > 0; i--) {
                sb.append("]");
                sb.insert(0, "[" + ((NumberNode) dimension.get(i)).number() + " x ");
            }
            sb.append("*");
            return sb.toString();
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
        } else if (refType == RefType.POINTER) {
            return 4;
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
