package com.teraime.poppyfield.room;

import android.app.Application;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Variable;
import com.teraime.poppyfield.gis.Geomatte;
import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.gis.PhotoMeta;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.loader.WebLoader;
import com.teraime.poppyfield.loader.parsers.JGWParser;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FieldPadRepository {

    private final VariableDAO mVDao;
    private final LiveData<List<VariableTable>> allVars;
    private final MutableLiveData<LatLngBounds> mBoundaries;
    private final Map<Fragment,LatLngBounds> boundaryMap;
    private final Map<String,String> columnKeyMap;
    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public FieldPadRepository(Application application) {
        FieldPadRoomDatabase db = FieldPadRoomDatabase.getDatabase(application);
        mVDao = db.variableDao();
        allVars = mVDao.getTimeOrderedList();
        mBoundaries = new MutableLiveData<>();
        boundaryMap = new HashMap<>();
        columnKeyMap = new HashMap<>();
        columnKeyMap.put("UUID","UUID");
        columnKeyMap.put("value","value");
        columnKeyMap.put("lag","lag");
        columnKeyMap.put("author","author");
        columnKeyMap.put("år","year");
        columnKeyMap.put("year","year");
        columnKeyMap.put("trakt","L1");
        columnKeyMap.put("gistyp","L2");
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

    public void insertGisObject(GisObject g) {
        Map<String,String> am = new HashMap<>();
        String var=GisConstants.GPS_Coord_Var_Name,value=g.coordsToString(),year="H";
        String colName;
        for(String key:g.getKeys().keySet()) {
            colName= columnKeyMap.get(key);
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

    public LiveData<LatLngBounds> getBoundary() {
        return mBoundaries;
    }

    public void updateBoundary(String app, String metaSource) {
        WebLoader.getMapMetaData(new LoaderCb() {
            @Override
            public void loaded(List<String> file) {
                PhotoMeta p=null;
                try {
                    p = JGWParser.parse(file,919,993);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                LatLng NE = Geomatte.convertToLatLong(p.E,p.N);
                LatLng SW = Geomatte.convertToLatLong(p.W,p.S);
                LatLngBounds latLngBounds = new LatLngBounds(SW,NE);
                mBoundaries.setValue(latLngBounds);
                Log.d("vortex","Observer informed");
            }
        }, app, metaSource);
    }
    private StringBuilder queryBase;
    public void buildQueryFromMap(Map<String,String> wfKeyMap) {
        queryBase = new StringBuilder();
        queryBase.append("SELECT value FROM variabler WHERE ");
        for (String k:wfKeyMap.keySet()) {
            String v = wfKeyMap.get(k);
            queryBase.append(k);
            queryBase.append(" = ");
            queryBase.append(v);
            queryBase.append(" AND ");
        }

        Log.d("queryBase", queryBase.toString());
    }
    public String latestMatch(String varName) {
        String queryString = queryBase.toString()+("var = "+varName);
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString);
        return mVDao.latestMatch(query).getValue();
    }

    public Variable latestMatch(SimpleSQLiteQuery query) {
        return new Variable(mVDao.latestMatch(query));
    }
}
