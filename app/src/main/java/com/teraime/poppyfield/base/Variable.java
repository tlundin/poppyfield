package com.teraime.poppyfield.base;

import com.teraime.poppyfield.room.VariableTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Variable {

    ValueProps mValues;
    Map<String,String> mVariableDef;


    public Variable(Map<String,String> variableDef, ValueProps props) {
        mVariableDef = variableDef;
        mValues = props;
    }

    public enum DataType {
        numeric,bool,list,text,existence,auto_increment, array, decimal
    }

    public String getId() {
        return mValues.getValue("UUID");
    }

    public ValueProps getValues() {
        return mValues;
    }
    public String getValue() {
        return mValues.getValue("value");
    }

    public DataType getType() {
        return DataType.valueOf(mVariableDef.get("Type"));
    }
}
