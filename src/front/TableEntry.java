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

public class TableEntry implements Operand {
    public enum ValueType {
        INT,
        VOID,
    }

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

    public TableEntry(RefType symbolType, ValueType valueType, String name, Integer initValue, boolean isConst,
                      int level) {
        this.refType = symbolType;
        this.valueType = valueType;
        this.name = name;
        this.initValue = new NumberNode(initValue);
        this.isConst = isConst;
        this.level = level;
    }

    public TableEntry(DefNode defNode, boolean isConst, int level, CompileUnit.Type type) {
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
}
