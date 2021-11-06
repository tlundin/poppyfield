package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.MaterialToolbar;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Context;
import com.teraime.poppyfield.base.DBHelper;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.GeoJsonGenerator;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.PageStack;
import com.teraime.poppyfield.base.Table;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.base.ValueProps;
import com.teraime.poppyfield.base.Variable;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldViewModel extends AndroidViewModel {
    private static WorldViewModel mStaticWorld;
    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private final MutableLiveData<String> loadState;
    private final String cachePath;
    private final ExecutorService mExecutorService;
    private final String app;
    private GoogleMap mMap;
    private final Application mActivity;
    private MaterialToolbar topAppBar;
    private final PageStack mPageStack;
    private final Loader mLoader;
    private MenuDescriptor mMenuDescriptor;
    private boolean appEntry = true;
    private SharedPreferences mAppPrefs,globalPrefs;
    private DBHelper mDBHelper;

    private Map<String, String> mEvalProps;
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private GisObject mTouchedGeoObject;

    public WorldViewModel(Application application) {
        super(application);
        globalPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        this.app=globalPrefs.getString("App","poppyfield");
        mAppPrefs = application.getSharedPreferences(app, android.content.Context.MODE_PRIVATE);
        mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        mRepository = new FieldPadRepository(application,mExecutorService);
        mLoader = new Loader();
        mPageStack = new PageStack(this);
        myConf = new MutableLiveData<>(mLoader.getConfigs());
        loadState = new MutableLiveData<>();
        cachePath = application.getFilesDir().getPath();
        mActivity=application;
        mStaticWorld = this;
    }

    public String getApp() { return app; }
    public ExecutorService getExecutor() { return mExecutorService; }

    //Livedata
    public LiveData<List<Config<?>>> getMyConf() { return myConf; }
    public LiveData<LatLngBounds> getMapBoundary() { return mRepository.getBoundary();}
    public LiveData<String> getLoadState() { return loadState; }
    public LiveData<Pair> getGeoJsonLD() { return mRepository.getJsonObjLD();}
    public WorkflowBundle getWorkFlowBundle() { return mLoader.getBundle(); }

    //DB functions
    public void deleteAllGisObjects() {
        mRepository.deleteAllHistorical();
    }
    public void deleteGisObjects(List<String> gisToDelete) { if (gisToDelete==null) deleteAllGisObjects(); else mRepository.deleteSomeHistorical(gisToDelete,mDBHelper.getColTranslator());}
    public void insert(VariableTable variable) { mRepository.insert(variable); }
    public void getBoundaryFromImage(String metaSource) {
                mRepository.updateBoundary(app, metaSource);
    }

    public void setMap(GoogleMap googleMap) {
        mMap = googleMap;
    }
    public GoogleMap getMap() {
        return mMap;
    }
    public BitmapDescriptor getmImgOverlay() { return  mRepository.getmImgOverlay();}


    public PageStack getPageStack() {
        return mPageStack;
    }

    public MutableLiveData<String> getLogObservable() {
       return mLoader.getLogObservable();
    }

    public void setToolBar(MaterialToolbar topAppBar) {
        this.topAppBar = topAppBar;
    }

    public MaterialToolbar getToolBar() {
        return topAppBar;
    }


    public String getCacheFolder() {
        return cachePath;
    }

    public Application getActivity() { return mActivity; }

    public MenuDescriptor getMenuDescriptor() {
        if (mMenuDescriptor == null)
            mMenuDescriptor = new MenuDescriptor(getWorkFlowBundle().getMainWf().getBlocks());
        else
            appEntry=false;
        return mMenuDescriptor;
    }

    public boolean isAppEntry() {return appEntry;}

    public List<GisType> getAllgeoData() { return mLoader.getGeoData();}
    public List<GisObject> getGeoDataType(String type) { return mLoader.getGeoDataType(type);}

    public void prepareGeoData() {
        MutableLiveData<String> logPing = getLogObservable();
        deleteGisObjects(mLoader.getGisFilesToLoad());
        mRepository.insertGisObjects(getAllgeoData(),mDBHelper.getColTranslator(),logPing);

    }


    public LiveData<JSONObject> queryGisObjects(Map<String, String> keyMap) {
        Log.d("GLERP", keyMap.toString());
        MutableLiveData<JSONObject> donePing = new MutableLiveData<>();
        LiveData<List<VariableTable>> mLoadCounter = mRepository.queryGisObjects(keyMap, mDBHelper.getColTranslator());
        Observer<List<VariableTable>> mObserver = load -> {
            Log.d("GIS","Recieved "+load+". Result has "+load.size());
            JSONObject jeo = load.size()>0?GeoJsonGenerator.print(load,mDBHelper.getColTranslator()):null;
            donePing.setValue(jeo);
        };
        mLoadCounter.observeForever(mObserver);

        return donePing;
    };

    public Map<String,String> getVariableExtraFields(String key) {
        return mLoader.getTable().getVariableExtraFields(key);
    }

    public Variable getVariable(String varName) {
        if (mEvalProps.containsKey(varName)) {
            Log.d("vagel","Found "+varName+" in evalProps!");
            return new Variable(mLoader.getTable().getVariableExtraFields(varName), new ValueProps().setValue(mEvalProps.get(varName)));
        }
        return null;
    }


    public SharedPreferences getAppPrefs() {
        return mAppPrefs;
    }

    public SharedPreferences getGlobalPrefs() {
        return globalPrefs;
    }



    public void setLoadState(String state) {
        loadState.setValue(state);
    }

    public void generateLayer(Block gisBlock, Context context) {
        String object_context = gisBlock.getAttr("obj_context");
        Log.d("obx_context","obj_context for "+gisBlock.getLabelExpr()+" is "+object_context);
        Log.d("obx_context","wf context for "+gisBlock.getLabelExpr()+" is "+((context==null)?"null":context.toString()));
        Map<String, String> gisLayerContext = Expressor.evaluate(Expressor.preCompileExpression(object_context),context);
        Log.d("layer","layer context is now "+gisLayerContext.toString());
        LiveData<JSONObject> geoLiveD = queryGisObjects(gisLayerContext);
        final Observer<JSONObject> mObserver = jsonObj -> {
            if (jsonObj == null)
                Log.d("generateLayer","Skipping layergen for "+gisBlock.getAttrs().get("label"));
            else
                mRepository.generateLayer(gisBlock,gisLayerContext,jsonObj);
        };
        geoLiveD.observeForever(mObserver);
    }


    public int getModuleCount() {
        if (mLoader.getGisFilesToLoad()!=null)
            return mLoader.getGisFilesToLoad().size();
        else
            return 0;
    }

    public void startLoad() {
        mLoader.load(app,this);
    }

    public void setAllGisTypesLoaded() {
        mLoader.markAllLoaded();
    }

    public void createDbHelper(Table t) {
        mDBHelper = new DBHelper(t.getColumnRealNames(),mAppPrefs);
    }

    public static WorldViewModel getStaticWorldRef() {
        return mStaticWorld;
    }

    public GisObject getSelectedGop() {
        return mTouchedGeoObject;
    }

    public void setSelectedGop(GisObject selected) {
        mTouchedGeoObject = selected;
    }

    public LiveData<Context> generateNewContext(Map<String,String> mKeyValues) {
        MutableLiveData<Context> ret = new MutableLiveData<Context>();
        final AtomicInteger loadC = new AtomicInteger(0);
        LiveData<List<VariableTable>> globs = mRepository.getGlobalVariables();
        LiveData<List<VariableTable>> vars = mRepository.getWorkflowVariables(mKeyValues,mDBHelper.getColTranslator());
        final Map<String, String> vMap = new HashMap<>();
        final Map<String, String> cMap = new HashMap<>();
        final Map<String, String> globMap = new HashMap<>();
        globs.observeForever(globTables -> {
            Log.d("GLOB","GLOB!");
            globMap.putAll(Tools.extractValues(globTables));
            if (loadC.incrementAndGet() > 1)
                ret.postValue(new Context(vMap,globMap,cMap));
        });

        vars.observeForever(varTables -> {
            if (varTables !=null && !varTables.isEmpty()) {
                vMap.putAll(Tools.extractValues(varTables));
                cMap.putAll(Tools.extractColumns(varTables.get(0)));
            }
            if (loadC.incrementAndGet() > 1)
                ret.postValue(new Context(vMap,globMap,cMap));
        });
        return ret;
    }




    public void setBoundaryFromCoordinates(LatLngBounds latLngBounds) { mRepository.setBoundary(latLngBounds); }

}

