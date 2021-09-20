package com.teraime.poppyfield.loader.parsers;

import static com.teraime.poppyfield.loader.parsers.StaticColumns.Col_Functional_Group;
import static com.teraime.poppyfield.loader.parsers.StaticColumns.Col_Variable_Name;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Tools;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsConfigurationParser {

    public static GroupsDescriptor parse(List<String> rows) throws ParseException {
        HashMap<String, List<List<String>>> groups = new HashMap<>();
        Logger.gl().d("PARSE","parsing groupsConfiguration. "+rows.size()+" lines");
        String[] groupsFileHeaderS = rows.remove(0).split(",");
        Logger.gl().d("PARSE","Header for Groups file: " + Arrays.toString(groupsFileHeaderS));
        int groupIndex=-1,nameIndex=-1;

        //Go through varpattern. Generate rows for the master table.
        //...but first - find the key columns in Artlista.
        //Find the Variable key row.
        for (int i = 0; i < groupsFileHeaderS.length; i++) {
            if (groupsFileHeaderS[i].trim().equals(Col_Functional_Group))
                groupIndex = i;
            else if (groupsFileHeaderS[i].trim().equals(Col_Variable_Name))
                nameIndex = i;
        }
        if (nameIndex == -1 || groupIndex == -1) {
            Logger.gl().e("GroupsConfiguration header missing either name or functional group column. Load cannot proceed");
            throw new ParseException("GroupsConfiguration header missing either name or functional group column. Load cannot proceed",-1);
        }
        int index=1;
        for (String row : rows) {
            //Split config file into parts according to functional group.
            String[] r = Tools.split(row);
            if (r.length > groupIndex) {
                for (int i = 0; i < r.length; i++) {
                    if (r[i] != null)
                        r[i] = r[i].replace("\"", "");
                }
                String group = r[groupIndex];
                //Add group if not already found
                List<List<String>> elem = groups.computeIfAbsent(group, k -> new ArrayList<>());
                elem.add(Arrays.asList(r));
            } else {
                Logger.gl().e("GroupsConfiguration has malformed row, rowindex "+index);
                throw new ParseException("GroupsConfiguration header missing either name or functional group column. Load cannot proceed",-1);

            }
            index++;
        }
        GroupsDescriptor gd = new GroupsDescriptor();
        gd.groupIndex=groupIndex;
        gd.nameIndex=nameIndex;
        gd.groups=groups;
        gd.groupsFileHeaderS=groupsFileHeaderS;
        return gd;
    }


    public static class GroupsDescriptor {
        String[] groupsFileHeaderS;
        Map<String, List<List<String>>> groups;
        int nameIndex,groupIndex;
    }
}


