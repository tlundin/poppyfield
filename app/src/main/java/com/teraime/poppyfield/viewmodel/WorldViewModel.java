package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import androidx.core.view.GravityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.MaterialToolbar;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.DBHelper;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.GeoJsonGenerator;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.PageStack;
import com.teraime.poppyfield.base.ValueProps;
import com.teraime.poppyfield.base.Variable;
import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.templates.LoadFragment;
import com.teraime.poppyfield.templates.Page;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldViewModel extends AndroidViewModel {


    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private final MutableLiveData<String> loadState;
    private final String cachePath;
    private final ExecutorService mExecutorService;
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;
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
    private Map<String,String> mWorkFlowContext,mCurrentGisLayerContext;
    private Map<String, String> mEvalProps;
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private int mManifestModuleCount;

    public WorldViewModel(Application application) {
        super(application);
        globalPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        this.app=globalPrefs.getString("App","smabio");
        mAppPrefs = application.getSharedPreferences(app, Context.MODE_PRIVATE);
        mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        mRepository = new FieldPadRepository(application,mExecutorService);
        mVariables = mRepository.getTimeOrderedList();
        mLoader = new Loader();
        mPageStack = new PageStack(this);
        myConf = new MutableLiveData<>(mLoader.getConfigs());
        loadState = new MutableLiveData<>();
        cachePath = application.getFilesDir().getPath();
        mActivity=application;
        mWorkFlowContext = new HashMap<>();
        mCurrentGisLayerContext = new HashMap<>();
    }

    public String getApp() { return app; }
    public List<String> getManifest(){ return mManifest; }
    public ExecutorService getExecutor() { return mExecutorService; }

    //Livedata
    public LiveData<List<VariableTable>> getAllVariables() { return mVariables; }
    public LiveData<List<Config<?>>> getMyConf() { return myConf; }
    public LiveData<LatLngBounds> getMapBoundary() { return mRepository.getBoundary();}
    public LiveData<String> getLoadState() { return (LiveData<String>) loadState; }
    public LiveData<Pair> getGeoJsonLD() { return (LiveData<Pair>)mRepository.getJsonObjLD();}
    public WorkflowBundle getWorkFlowBundle() { return mLoader.getBundle(); }

    //DB functions
    public void deleteAllGisObjects() {
        mRepository.deleteAllHistorical();
    }
    public void insert(VariableTable variable) { mRepository.insert(variable); }
    public void updateBoundary(String metaSource) {
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
        mDBHelper = new DBHelper(mLoader.getTable().getColumnRealNames(),mAppPrefs);
        deleteAllGisObjects();
        mRepository.insertGisObjects(getAllgeoData(),mDBHelper.getColTranslator(),logPing);

    }


    public LiveData<JSONObject> queryGisObjects(Map<String, String> keyMap) {
        Log.d("GLERP", keyMap.toString());
        MutableLiveData<String> mLoadCounter = new MutableLiveData<>();
        MutableLiveData<JSONObject> donePing = new MutableLiveData<>();
        final Map<String,List<VariableTable>> result = Collections.synchronizedMap(new HashMap<>());
        androidx.lifecycle.Observer<String> mObserver = load -> {
            Log.d("GIS","Recieved "+load+". Result has "+result.size());
            if (result.size() == 5) {
                Log.d("GIS","got all - generating json");
                JSONObject jeo = GeoJsonGenerator.print(result,mDBHelper.getColTranslator());
                donePing.setValue(jeo);
            }

        };
        mLoadCounter.observeForever(mObserver);
        mRepository.queryGisObjects(keyMap,mDBHelper.getColTranslator(),mLoadCounter,result);
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
        //StringBuilder queryBase = mRepository.buildQueryBaseFromMap(mWorkFlowContext,mDBHelper.getColTranslator());
        //String queryString = queryBase.append("var").append("=").append(varName).toString();
        //return new Variable(mLoader.getTable().getVariableExtraFields(varName),mRepository.latestMatchVariable(queryString).toMap());
    }

    //A context associated with a specific workflow.
    public void setCurrentWorkFlowContext(List<Expressor.EvalExpr> context) {
        mWorkFlowContext = Expressor.evaluate(context,getSingleObjectContext());
        Log.d("vagel","WF context set to "+mWorkFlowContext);
    }
    //A context associated with a gislayer.
    public Map<String, String> setCurrentGisLayerContext(List<Expressor.EvalExpr> context) {
        //TODO: This doesn't make sense
        mCurrentGisLayerContext = Expressor.evaluate(context,getWorkflowContext());
        if (mWorkFlowContext == null) {
            Log.d("vagel","MergedGisClontext DB1 "+mCurrentGisLayerContext);
            return mCurrentGisLayerContext;
        }
        Map<String, String> wfContext = new HashMap<>(mWorkFlowContext);
        if (mCurrentGisLayerContext == null) {
            mCurrentGisLayerContext = wfContext;
        } else {
            wfContext.forEach((key, value) -> mCurrentGisLayerContext.merge(key, value, (v1, v2) -> v1.equalsIgnoreCase(v2) ? v1 : v1));
        }
        Log.d("vagel","MergedGisClontext DB2 "+mCurrentGisLayerContext);
        return mCurrentGisLayerContext;
    }

    public void setSingleObjectContent(Map<String, String> properties) {

        mEvalProps = properties;
        if (mEvalProps !=null)
        Log.d("vagel","single obj props now "+mEvalProps.toString());
        else
            Log.d("vagel","sing obj props null");
    }



    public SharedPreferences getAppPrefs() {
        return mAppPrefs;
    }

    public SharedPreferences getGlobalPrefs() {
        return globalPrefs;
    }

    public void addKeyToContext(String key, String value) {
        String dbKey = mDBHelper.getColTranslator().ToDB(key);
        if (dbKey == null) {
            Logger.gl().e("Key Error in AddKey for key "+key);
            return;
        }
        mWorkFlowContext.put(dbKey,value);
    }
    public Map<String, String> getWorkflowContext() {
        return mWorkFlowContext;
    }


    public Map<String, String> getSingleObjectContext() {
        return mEvalProps;
    }

    public void setLoadState(String state) {
        loadState.setValue(state);
    }

    public void generateLayer(Block gisBlock) {
        String object_context = gisBlock.getAttr("obj_context");
        Map<String, String> gisLayerContext = Expressor.evaluate(Expressor.preCompileExpression(object_context),getSingleObjectContext());
        LiveData<JSONObject> geoLiveD = queryGisObjects(gisLayerContext);
        final Observer<JSONObject> mObserver = jsonObj -> {
            mRepository.generateLayer(gisBlock,getCacheFolder(),gisLayerContext,jsonObj);
        };
        geoLiveD.observeForever(mObserver);


    }


    public void setModuleCount(int i) {
        mManifestModuleCount = i;
    }

    public int getModuleCount() {
        return mManifestModuleCount;
    }

    public void startLoad() {
        mLoader.load(app,this);
    }
}

