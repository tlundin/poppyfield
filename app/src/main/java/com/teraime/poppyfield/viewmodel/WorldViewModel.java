package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.MaterialToolbar;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.DBHelper;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.PageStack;
import com.teraime.poppyfield.base.ValueProps;
import com.teraime.poppyfield.base.Variable;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldViewModel extends AndroidViewModel {


    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private final MutableLiveData<String> loadState;
    private final String cachePath;
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


    public WorldViewModel(Application application) {
        super(application);
        globalPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        this.app=globalPrefs.getString("App","smabio");
        mAppPrefs = application.getSharedPreferences(app, Context.MODE_PRIVATE);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();
        mLoader = new Loader();
        mPageStack = new PageStack(this);
        myConf = new MutableLiveData<>(mLoader.getConfigs());
        loadState = new MutableLiveData<>();
        cachePath = application.getFilesDir().getPath();
        mLoader.load(app,this);
        mActivity=application;
        mWorkFlowContext = new HashMap<>();
        mCurrentGisLayerContext = new HashMap<>();
    }

    public String getApp() { return app; }
    public List<String> getManifest(){ return mManifest; }

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
        mDBHelper = new DBHelper(mLoader.getTable().getColumnRealNames(),mAppPrefs);
        deleteAllGisObjects();
        mRepository.insertGisObjects(getAllgeoData(),mDBHelper.getColTranslator());
    }

    public Variable getVariable(String varName) {
        if (mEvalProps.containsKey(varName)) {
            Log.d("vagel","Found "+varName+" in evalProps!");
            return new Variable(mLoader.getTable().getVariableDef(varName), new ValueProps().setValue(mEvalProps.get(varName)));
        }
        return null;
        //StringBuilder queryBase = mRepository.buildQueryBaseFromMap(mWorkFlowContext,mDBHelper.getColTranslator());
        //String queryString = queryBase.append("var").append("=").append(varName).toString();
        //return new Variable(mLoader.getTable().getVariableDef(varName),mRepository.latestMatchVariable(queryString).toMap());
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
        mRepository.generateLayer(gisBlock,getCacheFolder(),getSingleObjectContext());
    }
}

