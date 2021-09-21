package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import java.util.List;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private final LiveData<List<Config<?>>> myConf;
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;
    private Workflow mWorkflow;
    private final String app;

    public WorldViewModel(Application application) {
        super(application);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();
        myConf = new MutableLiveData<>(Loader.getInstance().getConfigs());
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



    //DB functions
    public void deleteAllGisObjects() {
        mRepository.deleteAllHistorical();
    }
    public void insertGisObject(GisObject gi) {
        mRepository.insertGisObject(gi);
    }
    public void insert(VariableTable variable) { mRepository.insert(variable); }



}