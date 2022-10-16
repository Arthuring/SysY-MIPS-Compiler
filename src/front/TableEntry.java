package front;

import front.nodes.DefNode;
import front.nodes.ExprNode;
import front.nodes.FuncParamNode;
import front.nodes.NumberNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableEntry {
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

    public RefType refType;
    public ValueType valueType;
    public String name;
    public ExprNode initValue;
    public List<ExprNode> initValueList;
    public List<ExprNode> dimension;
    public int level;
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
}
