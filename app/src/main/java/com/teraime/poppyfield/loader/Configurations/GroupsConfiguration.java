package com.teraime.poppyfield.loader.Configurations;

import com.teraime.poppyfield.loader.parsers.GroupsConfigurationParser;

import java.text.ParseException;

public class GroupsConfiguration extends Config<GroupsConfiguration> {

    GroupsConfigurationParser.GroupsDescriptor mGroups;
    public GroupsConfiguration parse() throws ParseException {
        mGroups = GroupsConfigurationParser.parse(rawData);
        return this;
    }


}
