package com.teraime.poppyfield.room;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.DBHelper;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.gis.Geomatte;
import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.gis.PhotoMeta;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.loader.ImgLoaderCb;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.loader.WebLoader;
import com.teraime.poppyfield.loader.parsers.JGWParser;

import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FieldPadRepository {

    private final VariableDAO mVDao;
    //private final LiveData<List<VariableTable>> allVars;
    private final MutableLiveData<LatLngBounds> mBoundaries;
    private final MutableLiveData<Pair> jsonObjLD;
    private final LiveData<List<VariableTable>> mGlobalsLD;
    private BitmapDescriptor mImgOverlay;
    private final File cacheFolder;
    private final ExecutorService executorService;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples

    public FieldPadRepository(Application application, ExecutorService executorService) {
        FieldPadRoomDatabase db = FieldPadRoomDatabase.getDatabase(application);
        mVDao = db.variableDao();
        //allVars = mVDao.getTimeOrderedList();
        mBoundaries = new MutableLiveData<>();
        cacheFolder = new File(application.getFilesDir(), "cache");
        this.executorService = executorService;
        jsonObjLD   = new MutableLiveData<>();
        mGlobalsLD = mVDao.getAllGlobals();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    //public LiveData<List<VariableTable>> getTimeOrderedList() {return allVars;}

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public LiveData<String> insertChecked(VariableTable variable) {
        final MutableLiveData<String> doneM = new MutableLiveData<>();
        FieldPadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mVDao.insert(variable);
            doneM.postValue("DONE");
        });
        return doneM;
    }

    public void insert(VariableTable variable) {
        FieldPadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mVDao.insert(variable);
        });
    }

    public void deleteAllHistorical() {
        FieldPadRoomDatabase.databaseWriteExecutor.execute(mVDao::deleteAllHistorical);
    }

    public void deleteSomeHistorical(List<String> gisToDelete, DBHelper.ColTranslate colTranslate) {
        for (String gisType:gisToDelete) {
            SimpleSQLiteQuery query =
                    new SimpleSQLiteQuery("DELETE FROM variabler where year=='H' AND "+colTranslate.ToReal("gistyp")+"=='"+gisType+"'");
            FieldPadRoomDatabase.databaseWriteExecutor.execute(() -> {
                mVDao.deleteSomeHistorical(query);
            });
        }
    }

    public void insertGisObjects(GisObject g) {
        executorService.execute(() -> {mVDao.getAllGlobals();});
    }

    public void deleteAll() {
        FieldPadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mVDao.deleteAll();
        });
    }

    public LiveData<LatLngBounds> getBoundary() {
        return mBoundaries;
    }
    public BitmapDescriptor getmImgOverlay() {
        return mImgOverlay;
    }
    public MutableLiveData<Pair> getJsonObjLD() { return jsonObjLD; }

    public LiveData<List<VariableTable>> getGlobalVariables() { return mGlobalsLD; }

    public void updateBoundary(String app, String picName) {
        if (Tools.imageIsCached(cacheFolder,picName)) {
            Log.d("CACHE","loading "+picName+" from cache");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = BitmapFactory.decodeFile(cacheFolder.getPath()+"/"+ picName);
                    WebLoader.getMapMetaData(new LoaderCb() {
                        @Override
                        public void loaded(List<String> file) {
                            setBoundsFromJGwFile(file, bmp, picName);
                        }
                    }, app, picName);

                }
            });

        } else {
            WebLoader.getImage(new ImgLoaderCb() {
                @Override
                public void loaded(Bitmap bmp) {
                    WebLoader.getMapMetaData(new LoaderCb() {
                        @Override
                        public void loaded(List<String> file) {
                            setBoundsFromJGwFile(file, bmp, picName);
                        }
                    }, app, picName);

                }
            }, app, cacheFolder, picName);
        }
    }

    private void setBoundsFromJGwFile(List<String> file, Bitmap bmp, String picName) {
        if (file != null && bmp != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    PhotoMeta p = null;
                    try {
                        p = JGWParser.parse(file, bmp.getWidth(), bmp.getHeight());
                        LatLng NE = Geomatte.convertToLatLong(p.E, p.N);
                        LatLng SW = Geomatte.convertToLatLong(p.W, p.S);
                        LatLngBounds latLngBounds = new LatLngBounds(SW, NE);
                        Log.d("REPO", "creating overlay");
                        long t = System.currentTimeMillis();
                        mImgOverlay = BitmapDescriptorFactory.fromBitmap(bmp);
                        mBoundaries.postValue(latLngBounds);
                        Log.d("REPO", "time spent: " + (System.currentTimeMillis() - t));
                        if (Looper.getMainLooper().isCurrentThread()) {
                            Log.d("THREAD", "I AM ON UI THREAD");
                        } else
                            Log.d("THREAD", "I AM NOT ON UI THREAD");
                        Log.d("vortex", "Observer informed");
                    } catch (ParseException e) {
                        Logger.gl().e("Corrupted JGW metadata");
                    }
                }
            });
        } else {
            Logger.gl().d("JGW", "No image metadata for " + picName);
            mBoundaries.postValue(null);
        }

    }
    public LiveData<List<VariableTable>> getWorkflowVariables(Map<String, String> wfKeyMap, DBHelper.ColTranslate colTranslate) {
        if (wfKeyMap!=null) {
            StringBuilder sb = buildQueryBaseFromMap(wfKeyMap, colTranslate);
            return mVDao.rawVarQuery(new SimpleSQLiteQuery(sb.toString()));
        }
        return null;
    }


    public LiveData<List<VariableTable>> queryGisObjects(Map<String, String> wfKeyMap, DBHelper.ColTranslate colTranslate) {
        //coordinates are saved as variables. Need to add to gisvars that only contain properties
                            StringBuilder sb = buildQueryBaseFromMap(wfKeyMap, colTranslate);
                            sb.append(" AND var IN ( ");
                            Iterator<String> it = GisConstants.gisProperties.iterator();
                            while(it.hasNext()) {
                                String var = it.next();
                                sb.append("'");
                                sb.append(var);
                                sb.append("'");
                                if (it.hasNext())
                                    sb.append(", ");
                            }
                            sb.append(" )");
                            Log.d("SQL", "Query: " + sb.toString());
                            LiveData<List<VariableTable>> liveQuery = mVDao.rawVarQuery(new SimpleSQLiteQuery(sb.toString()));
        return liveQuery;
    }



    public StringBuilder buildQueryBaseFromMap(Map<String, String> wfKeyMap, DBHelper.ColTranslate colTranslate) {
        StringBuilder queryBase = new StringBuilder();
        queryBase.append("SELECT * FROM variabler WHERE ");
        if (wfKeyMap != null) {
            Iterator<String> it = wfKeyMap.keySet().iterator();
            while(it.hasNext()) {
                String k = it.next();
                String v = wfKeyMap.get(k);
                queryBase.append(colTranslate.ToDB(k));
                queryBase.append(" = '");
                queryBase.append(v);
                queryBase.append("'");
                if (it.hasNext())
                    queryBase.append(" AND ");
           }
        }
        Log.d("queryBase", queryBase.toString());
        return queryBase;
    }

    volatile int countGis=0;

    public void insertGisObjects(List<GisType> geoData, DBHelper.ColTranslate colTranslator, MutableLiveData<String> logPing) {
        countGis=0;
        final AtomicInteger gisObjsToInsert=new AtomicInteger(0);
        gisObjsToInsert.set(geoData.size());
        for (GisType gisType : geoData) {
            List<GisObject> geo = gisType.getGeoObjects();
            Iterator<GisObject> iterator = geo.iterator();
            while (iterator.hasNext()) {
            GisObject g = iterator.next();
                if (!iterator.hasNext()) {
                    LiveData<String> ld = insertGisObjectChecked(g, colTranslator);
                    Logger.gl().d("TIME", "Inserting " + geo.size() + " " + gisType.getType() );
                    ld.observeForever(new Observer<String>() {
                        @Override
                        public void onChanged(String ping) {
                            countGis++;
                            Log.d("INSERT", Integer.toString(countGis) + "targ " + gisObjsToInsert.toString());
                            if (countGis == gisObjsToInsert.get()) {
                                logPing.postValue("done");
                            } else
                                logPing.postValue(gisType.getType()+"("+countGis+"/"+gisObjsToInsert.toString()+")");
                        }
                    });
                } else
                    insertGisObject(g, colTranslator);
                logPing.postValue(gisType.getType());
            }
        }
    }
    private LiveData<String> insertGisObjectChecked(GisObject g, DBHelper.ColTranslate colTranslator) {
        return _insertGis(g,colTranslator,true);
    }
    private void insertGisObject(GisObject g, DBHelper.ColTranslate colTranslator) {
        _insertGis(g,colTranslator,false);
    }

    private LiveData<String> _insertGis(GisObject g, DBHelper.ColTranslate colTranslator,boolean tracked) {
        Map<String, String> am = new HashMap<>();
        String year = "H";
        String colName;
        for (String key : g.getKeys().keySet()) {
            colName = colTranslator.ToDB(key);
            if (colName == null)
                Logger.gl().e("missing column " + key);
            else
                am.put(colName, g.getKeys().get(key));
        }
        VariableTable vt;
        //Insert Attributes as Variables if they are supported.
        Map<String, String> attr = g.getAttributes();

        for (String key : attr.keySet()) {
            if (GisConstants.gisProperties.contains(key.toLowerCase(Locale.ROOT))) {
                vt = new VariableTable(0, am.get("UUID"), year, key, attr.get(key), null, null, -1, am.get("L1"), am.get("L2"), am.get("L3"), am.get("L4"), am.get("L5"), am.get("L6"), am.get("L7"), am.get("L8"), am.get("L9"), am.get("L10"));
                insert(vt);
            }
        }
        //Insert coordinates as a variable. Attach Observer if requested.
        vt = new VariableTable(0, am.get("UUID"), year, GisConstants.GPS_Coord_Var_Name, g.coordsToString(), null, null, -1, am.get("L1"), am.get("L2"), am.get("L3"), am.get("L4"), am.get("L5"), am.get("L6"), am.get("L7"), am.get("L8"), am.get("L9"), am.get("L10"));
        if (tracked)
            return insertChecked(vt);
        else {
            insert(vt);
            return null;
        }
    }


    public void generateLayer(Block gisBlock, Map<String, String> gisLayerContext, final JSONObject geoJsonData) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean createAllowed = gisBlock.getAttr("create_allowed").equals("true");
                //TODO: REMOVE
                if (!createAllowed) {
                    //Trakt + gistyp
                    String gisType = gisLayerContext.get("gistyp");
                    Log.d("GIPS","context "+gisLayerContext);
                    if (gisType != null) {
                            Pair<Block,JSONObject> pair;
                            if (geoJsonData == null) {
                                Log.d("genLayer","geojson null");
                                //long t = System.currentTimeMillis();
                                //File source = Paths.get(cacheFolder, "cache", gisType).toFile();
                                //pair = new Pair<>(gisBlock,new JSONObject(convert(source)));
                            } else {
                                pair = new Pair<>(gisBlock, geoJsonData);
                                jsonObjLD.postValue(pair);
                            }
                    }
                }

            }
        });
    }


    public void setBoundary(LatLngBounds latLngBounds) {
        mBoundaries.setValue(latLngBounds);
    }
}