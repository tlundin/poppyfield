package com.teraime.poppyfield.loader.Configurations;

import com.teraime.poppyfield.base.Table;
import com.teraime.poppyfield.loader.parsers.VariablesConfigurationParser;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class VariablesConfiguration extends Config<VariablesConfiguration> {

    Table t;
    public VariablesConfiguration parse(GroupsConfiguration gc) throws ParseException {
        t = VariablesConfigurationParser.parse(gc.mGroups,rawData);
        return this;
    }

    public Table getTable() {
        return t;
    }

    public final static String Col_Variable_Name = "Variable Name";
    public static final String Col_Variable_Keys = "Key Chain";
    public static final String Col_Variable_Label = "Variable Label";
    public static final String Type = "Type";
    public static final String Col_Functional_Group = "Group Name";
    public static final String Col_Variable_Scope = "Scope";
    public static final String Col_Variable_Limits = "Limits";
    public static final String Col_Variable_Dynamic_Limits = "D_Limits";
    public static final String Col_Group_Label = "Member Label";
    public static final String Col_Group_Description = "Member Description";

}
