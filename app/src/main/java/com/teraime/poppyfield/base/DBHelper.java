package com.teraime.poppyfield.base;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.GisType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DBHelper {

    private static final int NO_OF_KEYS = 10;
    private static String NONE = "_NULL_";
    private final Map<String,String> realToDBColumnName;
    private final Map<String,String> DBToRealColumnName;

    public Map<String, String> translate(Map<String, String> rawContext) {
        Map<String,String> ret = new HashMap();
        for (String key:rawContext.keySet()) {
            String dbKey = realToDBColumnName.get(key);
            ret.put(dbKey==null?key:dbKey,rawContext.get(key));
        }
        return ret;
    }


    public interface ColTranslate {
        String ToDB(String inAppColName);
        String ToReal(String DBColName);
    }

    public DBHelper(ArrayList<String> columnRealNames, SharedPreferences appPrefs) {
        realToDBColumnName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        DBToRealColumnName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

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
                Log.d("DBHelper", "Key " + columnRealNames.get(i) + " already exists with value "+realToDBColumnName.get(columnRealNames.get(i)));
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
            Log.d("DBHelper", "Key: " + e + " Value:" + realToDBColumnName.get(e));



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

    public class DBColumnPicker {
        final Cursor c;
        private static final String NAME = "var", VALUE = "value", TIMESTAMP = "timestamp", LAG = "lag", CREATOR = "author";

        DBColumnPicker(Cursor c) {
            this.c = c;
        }

        public StoredVariableData getVariable() {
            return new StoredVariableData(pick(NAME), pick(VALUE), pick(TIMESTAMP), pick(LAG), pick(CREATOR));
        }

        public Map<String, String> getKeyColumnValues() {
            Map<String, String> ret = new HashMap<String, String>();
            Set<String> keys = realToDBColumnName.keySet();
            String col = null;
            for (String key : keys) {
                col = realToDBColumnName.get(key);
                if (col == null)
                    col = key;
                if (pick(col)!= null)
                    ret.put(key, pick(col));
            }
            //Log.d("nils","getKeyColumnValues returns "+ret.toString());
            return ret;
        }

        private String pick(String key) {
            return c.getString(c.getColumnIndex(key));
        }

        public boolean moveToFirst() {
            return c != null && c.moveToFirst();
        }

        public boolean next() {
            boolean b = c.moveToNext();
            if (!b)
                c.close();
            return b;
        }

        public void close() {
            c.close();
        }

    }

    public class StoredVariableData {
        StoredVariableData(String name, String value, String timestamp,
                           String lag, String author) {
            this.timeStamp = timestamp;
            this.value = value;
            this.lagId = lag;
            this.creator = author;
            this.name = name;
        }

        public final String name;
        public final String timeStamp;
        public final String value;
        public final String lagId;
        public final String creator;
    }


}
