package com.teraime.poppyfield.base;

import android.content.SharedPreferences;
import android.util.Log;

import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.GisType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBHelper {

    private static final int NO_OF_KEYS = 10;
    private static String NONE = "_NULL_";
    private final Map<String,String> realToDBColumnName;
    private final Map<String,String> DBToRealColumnName;




    public interface ColTranslate {

        String ToDB(String inAppColName);

        String ToReal(String DBColName);
    }

    public DBHelper(ArrayList<String> columnRealNames, SharedPreferences appPrefs) {
        realToDBColumnName = new HashMap<>();
        DBToRealColumnName = new HashMap<>();

        realToDBColumnName.put("UUID","UUID");
        realToDBColumnName.put("uid","UUID");
        realToDBColumnName.put("value","value");
        realToDBColumnName.put("lag","lag");
        realToDBColumnName.put("author","author");
        realToDBColumnName.put("år","year");
        realToDBColumnName.put("year","year");

        int index = -1;
        for (int i = 1; i <= NO_OF_KEYS; i++) {
            String colKey = appPrefs.getString("L" + i, null);
            if (colKey==null) {
                Log.d("DBHelper", "didn't find key L" + i);
                index = i;
                break;
            } else {
                realToDBColumnName.put(colKey, "L" + i);
                DBToRealColumnName.put("L" + i, colKey);
            }
        }


        for (int i = 0; i < columnRealNames.size(); i++) {
            //Log.d("nils","checking keypart "+keyParts.get(i));
            if (realToDBColumnName.containsKey(columnRealNames.get(i)))
                Log.d("DBHelper", "Key " + columnRealNames.get(i) + " already exists..skipping");
            else {
                Log.d("DBHelper", "Found new column key " + columnRealNames.get(i));
                if (columnRealNames.get(i).isEmpty()) {
                    Log.d("DBHelper", "found empty keypart! Skipping");
                } else {
                    String colId = String.format("L%d", index++);
                    //Add key to memory
                    realToDBColumnName.put(columnRealNames.get(i), colId);
                    DBToRealColumnName.put(colId, columnRealNames.get(i));
                    //Persist new column identifier.
                    appPrefs.edit().putString(colId, columnRealNames.get(i)).apply();
                }
            }
        }
        Log.d("DBHelper", "Keys added: ");
        Set<String> s = realToDBColumnName.keySet();
        for (String e : s)
            Log.d("DBHelper", "Key: " + e + "Value:" + realToDBColumnName.get(e));



    }

    public ColTranslate getColTranslator() {
        return new ColTranslate() {
            @Override
            public String ToDB(String inAppColName) {
                return realToDBColumnName.get(inAppColName);
            }

            @Override
            public String ToReal(String DBColName) {
                return DBToRealColumnName.get(DBColName);
            }
        };
    }


}
