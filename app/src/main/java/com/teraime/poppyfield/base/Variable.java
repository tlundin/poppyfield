package com.teraime.poppyfield.base;

import android.util.ArraySet;
import android.util.Log;

import com.teraime.poppyfield.pages.Page;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variable {

    Context mContext;
    String mName;
    Table mTable;
    VariableConfiguration mVarConf;
    private boolean usingDefault;
    private boolean iAmOutOfRange=false;

    public Variable(String varName, Table t,Context context) {
        mName = varName;
        mContext = context;
        mTable = t;
    }

    public String getHistValue() {
        Page prev = WorldViewModel.getStaticWorldRef().getPageStack().getPreviousPage();
        if (prev!=null)
            return prev.getWorkflowContext().getVariableValues().get(mName);
        return null;
    }

    public void setValueWithDefault(String value) {
        usingDefault = true;
        mContext.getVariableValues().put(mName,value);
    }

    public Context getContext() {
        return mContext;
    }

    public String getLabel() {
        return getVariableConfiguration().getVarLabel();
    }

    public void deleteValue() {
        mContext.getVariableValues().put(mName,null);
    }

    public void setValue(String newValue) {
        mContext.getVariableValues().put(mName,newValue);
    }

    public boolean isContextVariable() {
        //TODO - implement
        return false;
    }

    public enum DataType {
        numeric,bool,list,text,existence,auto_increment, array, decimal
    }

    public String getId() {
        return mContext.getColumnValues().get("UUID");
    }

    public String getValue() {

        return mContext.getVariableValues().get(mName);

    }

    public boolean hasValueOutOfRange() {
        return iAmOutOfRange;
    }


    public void setOutOfRange(boolean oor) {
        iAmOutOfRange = oor;
    }

    public VariableConfiguration getVariableConfiguration() {
        if (mVarConf == null)
            mVarConf = new VariableConfiguration(mTable.getRowFromKey(mName));
        return mVarConf;
    }

    public DataType getType() {
        return getVariableConfiguration().getnumType();
    }

    public boolean isUsingDefault() {
        return usingDefault;
    }

    public enum Scope {
        local_sync,
        local_nosync,
        global_nosync,
        global_sync
    }

    public class VariableConfiguration {

        public  final String Col_Variable_Name = "Variable Name";
        private  final String Col_Variable_Label = "Variable Label";
        private  final String Col_Variable_Keys = "Key Chain";
        private  final String Type = "Type";
        public  final String Col_Functional_Group = "Group Name";
        private  final String Col_Variable_Scope = "Scope";
        private  final String Col_Variable_Limits = "Limits";
        private  final String Col_Variable_Dynamic_Limits = "D_Limits";
        private  final String Col_Group_Label = "Member Label";
        private  final String Col_Group_Description = "Member Description";
        private  final List<String> requiredColumns=Arrays.asList(Col_Variable_Keys,Col_Functional_Group,Col_Variable_Name,Col_Variable_Label,Type,"Unit","List Values","Description",Col_Variable_Scope,Col_Variable_Limits,Col_Variable_Dynamic_Limits,Col_Group_Label,Col_Group_Description);
        private  final int KEY_CHAIN=0;
        private  final int FUNCTIONAL_GROUP=1;
        private  final int VARIABLE_NAME=2;
        private  final int VARIABLE_LABEL=3;
        private  final int TYPE=4;
        private  final int UNIT=5;
        private  final int LIST_VALUES=6;
        private  final int DESCRIPTION=7;
        private  final int SCOPE=8;
        private  final int LIMIT=9;
        private  final int D_LIMIT=10;
        private  final int GROUP_LABEL=11;
        private  final int GROUP_DESCRIPTION = 12;

        private List<String> row;
        private Map<String,Integer>fromNameToColumn;

        public VariableConfiguration(List<String> row) {
            this.row = row;
            fromNameToColumn = new HashMap<String,Integer>();
            for (String c:requiredColumns) {
                int tableIndex = mTable.getColumnIndex(c);
                if (tableIndex==-1) {
                    Log.e("nils","Missing column: "+c);
                    Log.e("nils","Table has "+mTable.getColumnHeaders().toString());
                    return;
                }
                else
                    //Now we can map a call to a column to the actual implementation.
                    //Actual column index is decoupled.
                    fromNameToColumn.put(c, tableIndex);
            }
        }

        public String getAssociatedWorkflow() {
            return row.get(fromNameToColumn.get(requiredColumns.get(LIST_VALUES)));
        }
        public List<String> getListElements() {
            List<String> el = null;
            String listS = row.get(fromNameToColumn.get(requiredColumns.get(LIST_VALUES)));
            if (listS!=null&&listS.trim().length()>0) {
                String[] x = listS.trim().split("\\|");
                if (x!=null&&x.length>0)
                    el = new ArrayList<String>(Arrays.asList(x));
            }
            return el;
        }


        public String getVarName() {
            return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_NAME)));
        }

        public String getVarLabel() {
            return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_LABEL)));
        }

        public String getEntryLabel() {
            if (row == null)
                return null;
            String  res= mTable.getElement(Col_Group_Label, row);
            //If this is a non-art variable, use varlabel instead.
            if (res==null) {
                //Log.d("vortex","failed to find value for column "+Col_Group_Label+ ". Will use varlabel "+this.getVarLabel(row)+" instead.");
                //gs.getLogger().addRow("");
                //gs.getLogger().addYellowText("failed to find value for column "+Col_Group_Label+ ". Will use variable label "+this.getVarLabel(row)+" instead.");
                res =this.getVarLabel();
            }
            if (res == null)
                Log.e("nils","getEntryLabel failed to find a Label for row: "+row.toString());
            return res;
        }

        public String getVariableDescription() {
            return row.get(fromNameToColumn.get(requiredColumns.get(DESCRIPTION)));
        }


        public String getGroupDescription() {
            Integer col = fromNameToColumn.get(Col_Group_Description);
            if (col!=null && col<row.size())
                return row.get(col);
            return null;
        }

        public String getGroupLabel() {
            Integer col = fromNameToColumn.get(Col_Group_Label);
            if (col!=null && col<row.size())
                return row.get(col);
            return null;
        }

        //If the variable should be synchronized between the devices.
        public boolean isSynchronized() {
            String s= row.get(fromNameToColumn.get(requiredColumns.get(SCOPE)));
            return (s==null||s.length()==0||
                    s.equals(Scope.global_sync.name())||
                    s.equals(Scope.local_sync.name()));

        }

        //If the variable shall be exported via JSON to server.
        public boolean isLocal() {
            if (row!=null) {
                String s = row.get(fromNameToColumn.get(requiredColumns.get(SCOPE)));
                //Log.d("nils","getvarislocal uses string "+s);
                return (s != null && s.startsWith("local"));
            }
            Log.e("vortex","row was null...cannot determine if local or global. Will default to global");
            return false;


        }

        public String getLimitDescription() {
            return row.get(fromNameToColumn.get(requiredColumns.get(LIMIT)));
        }
        public String getDynamicLimitExpression() {
            return row.get(fromNameToColumn.get(requiredColumns.get(D_LIMIT)));
        }

        public String getKeyChain() {
            //Check for null or empty
            if (row==null) {
                Log.d("vortex","row was null in getKeyChain");
                return null;
            }
            Pattern pattern = Pattern.compile("\\s");
            Matcher matcher = pattern.matcher(row.get(0));
            if(matcher.find()) {
                Log.e("vortex","Space char found in keychain: "+row.get(0)+" length : "+row.get(0).length()+" size: "+row.size());
                return null;
            }

            else
                return row.get(fromNameToColumn.get(requiredColumns.get(KEY_CHAIN)));
        }

        public String getFunctionalGroup() {
            return row.get(fromNameToColumn.get(requiredColumns.get(FUNCTIONAL_GROUP)));
        }
        public Variable.DataType getnumType() {
            String type = row.get(fromNameToColumn.get(requiredColumns.get(TYPE)));
            if (type!=null) {
                if (type.equals("number")||type.equals("numeric"))
                    return Variable.DataType.numeric;
                else if (type.equals("boolean"))
                    return Variable.DataType.bool;
                else if (type.equals("list"))
                    return Variable.DataType.list;
                else if (type.equals("text")||type.equals("string"))
                    return Variable.DataType.text;
                else if (type.equals("auto_increment"))
                    return Variable.DataType.auto_increment;
                else if (type.equals("array"))
                    return Variable.DataType.array;
                else if (type.equals("decimal")||type.equals("float"))
                    return Variable.DataType.decimal;
                else
                    Log.e("nils","TYPE NOT KNOWN: ["+type+"]");
            }
            Logger.gl().e("Type parameter not configured for variable "+getVarName()+" Will default to numeric");
            return Variable.DataType.numeric;
        }


        public String getUnit() {
            String unit = row.get(fromNameToColumn.get(requiredColumns.get(UNIT)));
            if (unit == null) {
                Logger.gl().d("VAR","Unit was null for variable "+getVarName());
                unit = "";
            }
            return unit;
        }

        public String getUrl() {
            return mTable.getElement("Internet link", row);
        }

        public String getDescription() {
            String b = mTable.getElement(Col_Group_Description, row);
            if(b==null)
                b = this.getVariableDescription();

            return (b==null?"":b);
        }
    }
}

