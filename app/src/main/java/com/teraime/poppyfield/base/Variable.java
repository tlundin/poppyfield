package com.teraime.poppyfield.base;

import com.teraime.poppyfield.room.VariableTable;

public class Variable {

    VariableTable vt;


    public Variable(VariableTable vt) {
        this.vt = vt;
    }

    public enum DataType {
        numeric,bool,list,text,existence,auto_increment, array, decimal
    }

    public int getId() {
        return vt.getId();
    }

    public String getValue() {
        return vt.getValue();
    }

    public DataType getType() {
        return DataType.text;
    }
}
