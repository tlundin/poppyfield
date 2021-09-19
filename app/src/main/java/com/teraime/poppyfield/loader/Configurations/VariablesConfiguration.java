package com.teraime.poppyfield.loader.Configurations;

import com.teraime.poppyfield.base.Table;
import com.teraime.poppyfield.loader.parsers.VariablesConfigurationParser;

import java.text.ParseException;

public class VariablesConfiguration extends Config<VariablesConfiguration> {

    Table t;
    public VariablesConfiguration parse(GroupsConfiguration gc) throws ParseException {
        t = VariablesConfigurationParser.parse(gc.mGroups,rawData);
        return this;
    }

    public Table getTable() {
        return t;
    }

}
