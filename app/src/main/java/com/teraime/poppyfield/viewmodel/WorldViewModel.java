package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.MaterialToolbar;
import com.teraime.poppyfield.base.DBHelper;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.PageStack;
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
import java.util.TreeMap;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private final String cachePath;
    private final Expressor mExpressor;
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
    private Map<String,String> mWorkFlowContext,mSelectionBasedContext;

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
        cachePath = application.getFilesDir().getPath();
        mLoader.load(app,this);
        mExpressor = Expressor.create(this);
        mActivity=application;
        mWorkFlowContext = new HashMap<>();
        mSelectionBasedContext = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public String getApp() { return app; }
    public List<String> getManifest(){ return mManifest; }

    //Livedata
    public LiveData<List<VariableTable>> getAllVariables() { return mVariables; }
    public LiveData<List<Config<?>>> getMyConf() { return myConf; }
    public LiveData<LatLngBounds> getMapBoundary() { return mRepository.getBoundary();}
    public WorkflowBundle getWorkFlowBundle() { return mLoader.getBundle(); }


    //DB functions
    public void deleteAllGisObjects() {
        mRepository.deleteAllHistorical();
    }
    public void insert(VariableTable variable) { mRepository.insert(variable); }
    public void updateBoundary(String metaSource) {mRepository.updateBoundary(app, metaSource); }

    public void setMap(GoogleMap googleMap) {
        mMap = googleMap;
    }
    public GoogleMap getMap() {
        return mMap;
    }



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
        Long t1 = System.currentTimeMillis();
        mRepository.insertGisObjects(getAllgeoData(),mDBHelper.getColTranslator());
        Log.d("TIME","here after "+(System.currentTimeMillis()-t1)+" ms");
    }

    public Variable getVariable(String varName) {
        StringBuilder queryBase = mRepository.buildQueryBaseFromMap(mWorkFlowContext,mDBHelper.getColTranslator());
        String queryString = queryBase.append("var").append("=").append(varName).toString();
        return new Variable(mRepository.latestMatchVariable(queryString));
    }

    //A context associated with a specific workflow.
    public void setCurrentWorkFlowContext(List<Expressor.EvalExpr> context) {

        if (context != null) {
            Map<String, String> rawContext = mExpressor.evaluate(context);
            if (rawContext != null) {
                mWorkFlowContext = mDBHelper.translate(rawContext);
            }
        }

    }

    //A context associated with a specific workflow.
    public void setCurrentSelectionContext(Map<String,String> extraProps) {
            mSelectionBasedContext = extraProps;
    }
    public Map<String,String> getCurrentSelectionContext() {
        return mSelectionBasedContext;
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
    public Map<String, String> getCurrentContext() {
        return mWorkFlowContext;
    }
}

