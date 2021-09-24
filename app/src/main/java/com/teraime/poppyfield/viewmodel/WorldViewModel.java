package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.MaterialToolbar;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.templates.GisMapTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;
    private Workflow mWorkflow;
    private final String app;
    private GoogleMap mMap;
    private final Map<String, Fragment> mCurrentPage;
    private String infocusPage;
    private MaterialToolbar topAppBar;

    public WorldViewModel(Application application) {
        super(application);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();
        myConf = new MutableLiveData<>(Loader.getInstance().getConfigs());
        mCurrentPage = new HashMap<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        this.app=prefs.getString("App","smabio");
        Loader.getInstance().load(app,this);
    }

    public String getApp() { return app; }
    public List<String> getManifest(){ return mManifest; }
    public Workflow getSelectedWorkFlow() { return mWorkflow; }
    public void setSelectedWorkFlow(Workflow wf) { mWorkflow = wf; }
    //Livedata
    public LiveData<List<VariableTable>> getAllVariables() { return mVariables; }
    public LiveData<List<Config<?>>> getMyConf() { return myConf; }
    public LiveData<LatLngBounds> getMapBoundary(GisMapTemplate mapF) { return mRepository.getBoundary(mapF,app);}



    //DB functions
    public void deleteAllGisObjects() {
        mRepository.deleteAllHistorical();
    }
    public void insertGisObject(GisObject gi) {
        mRepository.insertGisObject(gi);
    }
    public void insert(VariableTable variable) { mRepository.insert(variable); }


    public void setMap(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public Fragment getPage(String page) {
        return mCurrentPage.get(page);
    }

    public String getInfocusPage() {
        return infocusPage;
    }
    public void setPage(Fragment page, String name) {
        mCurrentPage.put(name,page);
        infocusPage = name;
    }

    public MutableLiveData<String> getLogObservable() {
       return Loader.getInstance().getLogObservable();
    }

    public void setToolBar(MaterialToolbar topAppBar) {
        this.topAppBar = topAppBar;
    }

    public MaterialToolbar getToolBar() {
        return topAppBar;
    }
}
