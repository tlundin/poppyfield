package com.teraime.poppyfield.loader.parsers;

import static com.teraime.poppyfield.loader.parsers.StaticColumns.Col_Functional_Group;
import static com.teraime.poppyfield.loader.parsers.StaticColumns.Col_Variable_Name;

import android.util.Log;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Table;
import com.teraime.poppyfield.base.Tools;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VariablesConfigurationParser {

    private final static int VAR_PATTERN_ROW_LENGTH = 11;
    private final static int pNameIndex = 2;

    public enum ErrCode {
        tooManyColumns,
        tooFewColumns,
        keyError,
        ok
    }

    public static Table parse(GroupsConfigurationParser.GroupsDescriptor gc, List<String> rows) throws ParseException {
        Table myTable;
        List<String> cheaderL = new ArrayList<>();
        String header = rows.remove(0);
        Logger.gl().d("PARSE", "VariablesConfig header: " + header);
        String[] varPatternHeaderS = header.split(",");
        if (varPatternHeaderS.length < VAR_PATTERN_ROW_LENGTH) {
            Logger.gl().e("PARSE", "Header corrupt in Variables.csv: " + Arrays.toString(varPatternHeaderS));
            throw new ParseException("Corrupt header", 0);
        }
        //Remove duplicte group column and varname if group file present.
        if (gc != null) {
            boolean foundFunctionalGroupHeader = false, foundVarNameHeader = false;
            Collections.addAll(cheaderL, gc.groupsFileHeaderS);
            Log.d("vortex", "header now " + cheaderL.toString());
            Iterator<String> it = cheaderL.iterator();
            while (it.hasNext()) {
                header = it.next();
                if (header.equals(Col_Functional_Group)) {
                    Log.d("vortex", "found column Functional Group ");
                    foundFunctionalGroupHeader = true;
                    it.remove();
                } else if (header.equals(Col_Variable_Name)) {
                    Log.d("vortex", "found column VariableName");
                    foundVarNameHeader = true;
                    it.remove();
                }
            }
            if (!foundFunctionalGroupHeader || !foundVarNameHeader) {
                Logger.gl().e("PARSE","Could not find required columns " + Col_Functional_Group + " or " + Col_Variable_Name);
                throw new ParseException("VariableConfiguration - Corrupt header ",0);
            }

        }
        List<String> vheaderL = new ArrayList<>(trimmed(varPatternHeaderS));
        vheaderL.addAll(cheaderL);
        myTable = new Table(vheaderL, 0, pNameIndex);
        int index = 1;
        for (String row : rows) {
            List<List<String>> elems;
            String[] r = Tools.split(row);

            if (r.length < VAR_PATTERN_ROW_LENGTH) {
                Logger.gl().e("PARSE","Too short row or row null in Variable.csv.");
                Logger.gl().e("PARSE","Row length: " + r.length + ". Expected length: " + VAR_PATTERN_ROW_LENGTH);
                throw new ParseException("Parse error, row: "+index,index);
            } else {
                for (int i = 0; i < r.length; i++) {
                    if (r[i] != null)
                        r[i] = r[i].replace("\"", "");
                }
                int pGroupIndex = 1;
                String pGroup = r[pGroupIndex];
                List<String> trr = trimmed(r);
                if (pGroup == null || pGroup.trim().length() == 0) {
                    //Log.d("nils","found variable "+r[pNameIndex]+" in varpattern");
                    myTable.addRow(trr);
                    //o.addRow("Generated variable(1): ["+r[pNameIndex]+"]");
                    //Log.d("vortex", "Generated variable [" + r[pNameIndex] + "] ROW:\n" + row);
                } else {
                    //Log.d("nils","found group name: "+pGroup);
                    elems = gc.groups.get(pGroup);
                    String varPatternName = r[pNameIndex];
                    if (elems == null) {
                        //If the variable has a group,add it
                        //Log.d("nils","Group "+pGroup+" in line#"+rowC+" does not exist in config file. Will use name: "+varPatternName);
                        String name = pGroup.trim() + ":" + varPatternName.trim();
                        //o.addRow("Generated variable(2): ["+name+"]");
                        trr.set(pNameIndex, name);
                        myTable.addRow(trr);
                    } else {
                        for (List<String> elem : elems) {
                            //Go through all rows in group. Generate variables.
                            String cFileNamePart = elem.get(gc.nameIndex);

                            if (varPatternName == null) {
                                Logger.gl().e("PARSE","varPatternNamepart evaluates to null at line#" + index + " in varpattern file");
                            } else {
                                String fullVarName = pGroup.trim() + ":" + (cFileNamePart != null ? cFileNamePart.trim() + ":" : "") + varPatternName.trim();
                                //Remove duplicate elements from Config File row.
                                //Make a copy.
                                List<String> elemCopy = new ArrayList<>(elem);
                                elemCopy.remove(gc.nameIndex);
                                elemCopy.remove(gc.groupIndex);
                                List<String> varPatternL = new ArrayList<>(trimmed(r));
                                varPatternL.addAll(elemCopy);
                                //Replace name column with full name.
                                varPatternL.set(pNameIndex, fullVarName);
                                //o.addRow("Generated variable(3): ["+fullVarName+"]");
                                ErrCode err = myTable.addRow(varPatternL);
                                if (err != ErrCode.ok) {
                                    switch (err) {
                                        case keyError:

                                            Logger.gl().e("PARSE","KEY ERROR!");
                                            break;
                                        case tooFewColumns:

                                            Logger.gl().e("PARSE","TOO FEW COLUMNS!");
                                            throw new ParseException("Too few columns",0);
                                        case tooManyColumns:

                                            Logger.gl().e("PARSE","TOO MANY COLUMNS!");
                                            Logger.gl().e("PARSE","VariablesConfiguration, line " + index);
                                            break;
                                    }

                                }
                            }
                            index++;
                        }
                    }
                }
            }

        }


        return myTable;
    }

    private static List<String> trimmed(String[] r) {
        return new ArrayList<>(Arrays.asList(r).subList(0, VAR_PATTERN_ROW_LENGTH));
    }
}
