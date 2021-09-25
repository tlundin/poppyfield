package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.MaterialToolbar;
import com.teraime.poppyfield.base.PageStack;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import java.util.List;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private final String cachePath;
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;
    private final String app;
    private GoogleMap mMap;

    private MaterialToolbar topAppBar;
    private PageStack mPageStack;
    private final Loader mLoader;

    public WorldViewModel(Application application) {
        super(application);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();
        mLoader = new Loader();
        mPageStack = new PageStack(this);
        myConf = new MutableLiveData<>(mLoader.getConfigs());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        this.app=prefs.getString("App","smabio");
        cachePath = application.getFilesDir().getPath();
        mLoader.load(app,this);
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
    public void insertGisObject(GisObject gi) {
        mRepository.insertGisObject(gi);
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
}

