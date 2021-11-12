package com.teraime.poppyfield.base;

import com.teraime.poppyfield.base.Context;
import android.util.Log;
import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1965204853256767316L;
    private final String target;
    private String action;
    private final String errorMsg;
    private final String label;
    private final String id;
    private Expressor.EvalExpr condition;
    private Context ctx;
    private Type myType;
    private boolean initDone = false;
    private Logger o;
    private int myTargetBlockId=-1;
    private final String conditionS=null;
    //Old rule engine for back compa.
    private boolean oldStyle = false;

    public Rule(String id, String ruleLabel, String target, String condition,
                String action, String errorMsg) {

        this.label=ruleLabel;
        this.id=id;
        this.target=target;
        //
        List<Expressor.EvalExpr> tmp = Expressor.preCompileExpression(condition);
        if (tmp!=null) {
            this.condition = tmp.get(0);
            Log.d("vortex", "Bananas rule " + condition);
        } else
            Log.d("vortex", "Condition precompiles to null: "+condition);
        this.errorMsg=errorMsg;
        myType = Type.WARNING;
        if (action!=null && action.equalsIgnoreCase("Error_severity"))
            myType = Type.ERROR;
        try {
            myTargetBlockId = Integer.parseInt(target);
        } catch (NumberFormatException e) {}
    }

   public String getTargetString() {
        return target;
    }


    public enum Type {
        ERROR,
        WARNING
    }



    //Execute Rule. Target will be colored accordingly.
    public Boolean execute(Context mContext) {
       if (condition!=null) {
    	   System.err.println("CALLING BOOL ANALYSIS WITH "+condition.toString());
           return Expressor.analyzeBooleanExpression(condition, mContext);
       } 
       return false;
    }

    public String getRuleText() {
        return errorMsg;
    }

    public String getRuleHeader() {
        return label;
    }

    public Type getType() {
        return myType;
    }

    public int getMyTargetBlockId() {
        return myTargetBlockId;
    }

    public String getId() {
        return id;
    }
    public String getCondition() {
        return conditionS;
    }

}
