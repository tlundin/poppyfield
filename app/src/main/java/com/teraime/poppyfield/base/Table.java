package com.teraime.poppyfield.base;

import android.util.Log;

import com.teraime.poppyfield.loader.parsers.VariablesConfigurationParser.ErrCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Table {

    private static final long serialVersionUID = 1183209171210448314L;
    //The table is a Map of key=Header,value=List of Data.
    private final Map<String,List<String>> colTable=new TreeMap<String,List<String>>(String.CASE_INSENSITIVE_ORDER);
    private final Map<Integer,List<String>> rowTable=new HashMap<Integer,List<String>>();
    private final Map<String,List<String>> nameToRowMap=new TreeMap<String,List<String>>(String.CASE_INSENSITIVE_ORDER);
    private final ArrayList<String> keyParts = new ArrayList<String>();
    //Immutable list of Required columns.
    private int rowCount=0,keyChainIndex =-1;
    private List<String> myColumns;
    private String previousKeyChain = null;
    private int variableIdIndex=-1;

    public Table (List<String> columnNames,int keyChainIndex,int nameIndex) {
        myColumns = columnNames;
        for(String key:columnNames)
            colTable.put(key, new ArrayList<String>());
        this.keyChainIndex = keyChainIndex;
        this.variableIdIndex = nameIndex;
        myColumns = columnNames;
    }


    public ErrCode addRow(List<String> rowEntries) {
        int index=0;
        if (rowEntries == null||rowEntries.size()==0)
            return ErrCode.tooFewColumns;
        int size = rowEntries.size();
        if (size > myColumns.size()) {
            Log.e("nils","TOO MANY: ");
            Log.e("nils","RowEntries: "+rowEntries.toString());
            Log.e("nils","myColumns: "+myColumns.toString());
            Log.e("nils","RowEntries s: "+rowEntries.size());
            Log.e("nils","myColumns s: "+myColumns.size());

            return ErrCode.tooManyColumns;
        }
        //columnmap
        for(String entry:rowEntries)
            colTable.get(myColumns.get(index++)).add(entry);
        //rowmap
        rowTable.put(rowCount++, rowEntries);
        //keymap.
        nameToRowMap.put(rowEntries.get(variableIdIndex),rowEntries);
        //Check keychain and add
        if (rowEntries.size()<keyChainIndex) {
            Log.e("nils","row length shorter than key index");
            return ErrCode.tooFewColumns;
        }
        String keyChain = rowEntries.get(keyChainIndex);


        //check if any new key
        //if equal to previous, skip
        if (!keyChain.equals(previousKeyChain)) {
            String[] keys = keyChain.split("\\|");
            if (keys == null) {
                Log.e("nils","KeyChain null after split");
                return ErrCode.keyError;
            }
            for (String key:keys) {
                if (!keyParts.contains(key)&&key.trim().length()>0) {
                    //Log.d("nils","found new key part: "+key);
                    //Add to existing Database model.
                    keyParts.add(key.trim());
                }
            }
            //no need to check this one again.
            previousKeyChain = keyChain;
        }

        return ErrCode.ok;
    }

    public int getColumnIndex(String c) {
        //Log.d("vortex","My columns: "+myColumns.toString());
        for (int i=0;i<myColumns.size();i++)
            if (c.equalsIgnoreCase(myColumns.get(i)))
                return i;
        return -1;
    }

    public List<String> getColumnHeaders() {
        return myColumns;
    }

    public ArrayList<String> getColumnRealNames() {
        return keyParts;
    }

    public List<String> getRowFromKey(String key) {
        if (key == null) {
            Log.e("nils","key was null in getRowFromKey (Table.java)");
            return null;
        }
        return nameToRowMap.get(key.trim());
    }

    public Map<String, String> getVariableExtraFields(String key) {
        List<String> row = getRowFromKey(key);
        if (row !=null) {
            Map<String, String> ret = new HashMap<String, String>();
            for(int i=0;i<row.size();i++) {
                ret.put(myColumns.get(i),row.get(i));
            }
            return ret;
        }
        return null;
    }


    public void printTable() {
        Log.d("vortex",myColumns.toString());
        for (Integer key:rowTable.keySet()) {
            List<String> l = rowTable.get(key);
            Log.d("vortex","Row: "+key+":");
            Log.d("vortex",l.toString());
        }

    }

    public String getElement(String columnName,List<String> row) {
        String result = null;
        int index = getColumnIndex(columnName);
        if (index !=-1) {
            if (row.size()>index)
                result = row.get(index);
            //Log.d("nils","found field "+columnName+": "+result+" in class Table");
        } else {
            Logger o = Logger.gl();
            o.e("Did not find column named "+columnName);
            Log.e("nils","Did NOT find field ["+columnName+"] in class Table. Columns available:");
            for (int i=0;i<myColumns.size();i++)
                Log.e("vortex","["+myColumns.get(i)+"]");
        }
        return result;
    }
}
