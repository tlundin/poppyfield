package com.teraime.poppyfield.room;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.gis.GisObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FieldPadRepository {

    private final VariableDAO mVDao;
    private final LiveData<List<VariableTable>> allVars;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public FieldPadRepository(Application application) {
        FieldPadRoomDatabase db = FieldPadRoomDatabase.getDatabase(application);
        mVDao = db.variableDao();
        allVars = mVDao.getTimeOrderedList();

        map.put("UUID","UUID");
        map.put("value","value");
        map.put("lag","lag");
        map.put("author","author");
        map.put("år","year");
        map.put("year","year");
        map.put("trakt","L1");
        map.put("gistyp","L2");
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<VariableTable>> getTimeOrderedList() {
        return allVars;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(VariableTable variable) {
        FieldPadRoomDatabase.databaseWriteExecutor.execute(() -> mVDao.insert(variable));
    }

    public void deleteAllHistorical() {
        FieldPadRoomDatabase.databaseWriteExecutor.execute(mVDao::deleteAllHistorical);
    }

    Map<String,String> map = new HashMap<>();


    public void insertGisObject(GisObject g) {
        Map<String,String> am = new HashMap<>();
        String var=GisConstants.GPS_Coord_Var_Name,value=g.coordsToString(),year="H";
        String colName;
        for(String key:g.getKeys().keySet()) {
            colName=map.get(key);
            if (colName==null)
                Logger.gl().e("missing key "+key);
            else
                am.put(colName,g.getKeys().get(key));
        }
        VariableTable vt = new VariableTable(0,am.get("UUID"),year,var,value,null,null,-1,am.get("L1"),am.get("L2"),am.get("L3"),am.get("L4"),am.get("L5"),am.get("L6"),am.get("L7"),am.get("L8"),am.get("L9"),am.get("L10"));
        insert(vt);
        //insert all attributes.
        Map<String, String> attr = g.getAttributes();
        Set<String> wanted = new HashSet<String>();
        wanted.add("geotype");
        wanted.add("gistyp");
        wanted.add("objektid");
        wanted.add("subgistyp");
        for (String key:attr.keySet()) {
            if (wanted.contains(key)) {
                vt = new VariableTable(0,am.get("UUID"),year,key,attr.get(key),null,null,-1,am.get("L1"),am.get("L2"),am.get("L3"),am.get("L4"),am.get("L5"),am.get("L6"),am.get("L7"),am.get("L8"),am.get("L9"),am.get("L10"));
                insert(vt);
            }
        }
    }

    public void deleteAll() {
        FieldPadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mVDao.deleteAll();
        });
    }

}