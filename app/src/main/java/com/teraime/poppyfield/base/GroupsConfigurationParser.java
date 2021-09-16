package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupsConfigurationParser {
/*
    public static GroupsConfiguration parse(String row, Integer currentRow) {
        //Log.d("vortex","group parsing "+row);
        //if no header, abort.
        if (scanHeader && row == null) {
            o.addRow("");
            o.addRedText("Header missing. Load cannot proceed");
            return new LoadResult(this,ErrorCode.ParseError);
        }
        //Scan header.
        if (scanHeader && row!=null) {
            Log.d("vortex","Header for groups is "+row);

            groupsFileHeaderS = row.split(",");
            o.addRow("Header for Groups file: "+row);
            o.addRow("Has: "+groupsFileHeaderS.length+" elements");
            scanHeader = false;
            //Go through varpattern. Generate rows for the master table.
            //...but first - find the key columns in Artlista.


            //Find the Variable key row.
            for (int i = 0; i<groupsFileHeaderS.length;i++) {
                if (groupsFileHeaderS[i].trim().equals(VariableConfiguration.Col_Functional_Group))
                    groupIndex = i;
                else if  (groupsFileHeaderS[i].trim().equals(VariableConfiguration.Col_Variable_Name))
                    nameIndex = i;
            }

            if (nameIndex ==-1 || groupIndex == -1) {
                o.addRow("");
                o.addRedText("Header missing either name or functional group column. Load cannot proceed");
                o.addRow("Header:");
                o.addRow(row);
                return new LoadResult(this,ErrorCode.ParseError);
            }
        } else {
            //Split config file into parts according to functional group.
            String[] r = Tools.split(row);
            if (r!=null && r.length>groupIndex) {
                for(int i=0;i<r.length;i++) {
                    if (r[i]!=null)
                        r[i] = r[i].replace("\"", "");
                }
                String group = r[groupIndex];
                //Add group if not already found
                List<List<String>> elem = groups.get(group);
                if (elem==null) {
                    elem = new ArrayList<List<String>>();
                    groups.put(group,elem);
                }
                elem.add(Arrays.asList(r));
            } else {
                o.addRow("");
                o.addRedText("Impossible to split row #"+currentRow);
                o.addRow("ROW that I cannot parse:");
                o.addRow(row);
                return new LoadResult(this,ErrorCode.ParseError);
            }
        }
        return null;
    }

 */
}
