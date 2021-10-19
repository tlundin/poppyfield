package com.teraime.poppyfield.room;

import static com.teraime.poppyfield.gis.Geomatte.convert;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.DBHelper;
import com.teraime.poppyfield.base.Expressor;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FieldPadRepository {

    private static final int DEFAULT_THREAD_POOL_SIZE = 4;
    private final VariableDAO mVDao;
    private final LiveData<List<VariableTable>> allVars;
    private final MutableLiveData<LatLngBounds> mBoundaries;
    private final MutableLiveData<Pair> jsonObjLD;
    private final Map<Fragment, LatLngBounds> boundaryMap;
    private BitmapDescriptor mImgOverlay;
    private final File cacheFolder;
    private final ExecutorService executorService;

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
        cacheFolder = new File(application.getFilesDir(), "cache");
        executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        jsonObjLD   = new MutableLiveData<>();
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

    public void insertGisObjects(GisObject g) {

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
        } else
            Logger.gl().d("JGW", "No image metadata for " + picName);

    }





    public StringBuilder buildQueryBaseFromMap(Map<String, String> wfKeyMap, DBHelper.ColTranslate colTranslate) {
        StringBuilder queryBase = new StringBuilder();
        queryBase.append("SELECT value FROM variabler WHERE ");
        if (wfKeyMap != null) {
            for (String k : wfKeyMap.keySet()) {
                String v = wfKeyMap.get(k);
                queryBase.append(colTranslate.ToDB(k));
                queryBase.append(" = ");
                queryBase.append(v);
                queryBase.append(" AND ");
            }
        }
        Log.d("queryBase", queryBase.toString());
        return queryBase;
    }

    public String latestMatchValue(String queryString) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString);
        return mVDao.latestMatch(query).getValue();
    }

    public VariableTable latestMatchVariable(String queryString) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString);
        return mVDao.latestMatch(query);
    }

    public void insertGisObjects(List<GisType> geoData, DBHelper.ColTranslate colTranslator) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (GisType gisType : geoData) {
                        long t1 = System.currentTimeMillis();
                        List<GisObject> geo = gisType.getGeoObjects();
                        for (GisObject g : geo)
                            insertGisObject(g, colTranslator);
                        long diff = (System.currentTimeMillis() - t1);
                        Logger.gl().d("TIME", "Inserted " + geo.size() + " " + gisType.getType() + " in " + diff + " millsec");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void insertGisObject(GisObject g, DBHelper.ColTranslate colTranslator) {
        Map<String, String> am = new HashMap<>();
        String var = GisConstants.GPS_Coord_Var_Name, value = g.coordsToString(), year = "H";
        String colName;
        for (String key : g.getKeys().keySet()) {
            colName = colTranslator.ToDB(key);
            if (colName == null)
                Logger.gl().e("missing key " + key);
            else
                am.put(colName, g.getKeys().get(key));
        }
        VariableTable vt = new VariableTable(0, am.get("UUID"), year, var, value, null, null, -1, am.get("L1"), am.get("L2"), am.get("L3"), am.get("L4"), am.get("L5"), am.get("L6"), am.get("L7"), am.get("L8"), am.get("L9"), am.get("L10"));
        insert(vt);
        //insert all attributes.
        Map<String, String> attr = g.getAttributes();
        Set<String> wanted = new HashSet<String>();
        wanted.add("geotype");
        wanted.add("gistyp");
        wanted.add("objektid");
        wanted.add("subgistyp");
        for (String key : attr.keySet()) {
            if (wanted.contains(key)) {
                vt = new VariableTable(0, am.get("UUID"), year, key, attr.get(key), null, null, -1, am.get("L1"), am.get("L2"), am.get("L3"), am.get("L4"), am.get("L5"), am.get("L6"), am.get("L7"), am.get("L8"), am.get("L9"), am.get("L10"));
                insert(vt);
            }
        }
    }

    public void generateLayer(Block gisBlock, String cacheFolder, Map<String,String> wfContext) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean createAllowed = gisBlock.getAttr("create_allowed").equals("true");
                //TODO: REMOVE
                if (!createAllowed) {
                    String object_context = gisBlock.getAttr("obj_context");
                    //Trakt + gistyp
                    Map<String, String> gisLayerContext = Expressor.evaluate(Expressor.preCompileExpression(object_context),wfContext);
                    String gisType = gisLayerContext.get("gistyp");
                    Log.d("GIPS","context "+gisLayerContext);
                    if (gisType != null) {
                        try {
                            //Create JSON here
                            long t = System.currentTimeMillis();
                            File source = Paths.get(cacheFolder, "cache", gisType).toFile();
                            JSONObject geoJsonData = new JSONObject(convert(source));
                            Pair<Block,JSONObject> pair = new Pair<>(gisBlock,geoJsonData);

                            jsonObjLD.postValue(pair);
                            Log.d("TIME","CreateLayer Here after "+(System.currentTimeMillis()-t));
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        });
    }
}