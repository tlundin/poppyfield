package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Configurations.GroupsConfiguration;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.loader.Configurations.VariablesConfiguration;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.loader.WebLoader;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private final LiveData<List<Config>> myConf;
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;

    public WorldViewModel(Application application) {
        super(application);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();
        myConf = new MutableLiveData<List<Config>>(Loader.getInstance().getConfigs());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        String app=prefs.getString("App","smabio");
        Loader.getInstance().load(app,this);
    }


    public List<String> getManifest(){ return mManifest; }
    //Livedata
    public LiveData<List<VariableTable>> getAllVariables() { return mVariables; }
    public LiveData<List<Config>> getMyConf() { return myConf; }



    //DB functions
    public void deleteAllGisObjects() {
        mRepository.deleteAllHistorical();
    }
    public void insertGisObject(GisObject gi) {
        mRepository.insertGisObject(gi);
    }
    public void insert(VariableTable variable) { mRepository.insert(variable); }

}