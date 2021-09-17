package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.loader.Configurations.GroupsConfiguration;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.loader.Configurations.VariablesConfiguration;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import java.util.ArrayList;
import java.util.List;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private MutableLiveData<Logger> mLoadLog;
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;

    public WorldViewModel(Application application) {
        super(application);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        //Load manifest
        String app=prefs.getString("App","smabio");
        List<List<String>> geoJsonFiles = new ArrayList<>();

        Loader.getManifest(fileList -> {
            mManifest = (List<String>)fileList;
            Loader.loadGisModules(new LoaderCb() {
                @Override
                public void loaded(List<String> _d) {
                    int i=0;
                    List<GisType> gisTypeL = new ArrayList<>();
                    for (List<String>geoJ:geoJsonFiles) {
                        try {
                            long t1 = System.currentTimeMillis();
                            String type = mManifest.get(i++);
                            GisType gf = new GisType();
                            gisTypeL.add(gf.strip(geoJ).stringify().parse(type));
                            long diff = (System.currentTimeMillis()-t1);
                            Logger.gl().d("PARSE","Parsed "+type+"("+gf.getVersion()+") in "+diff+" millsec");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Logger.gl().d("INSERT","DELETE ALL CALLED");
                    mRepository.deleteAll();
                    for (GisType gisType : gisTypeL) {
                        long t1 = System.currentTimeMillis();
                        List<GisObject> geo = gisType.getGeoObjects();
                        for (GisObject g:geo)
                            mRepository.insert(g);
                        long diff = (System.currentTimeMillis()-t1);
                        Logger.gl().d("INSERT","Inserted "+geo.size()+" "+ gisType.getType()+" in "+diff+" millsec");
                    }
                    Logger.gl().d("INSERT","DONE.");
                    mLoadLog.postValue(Logger.gl());
                }
            },mManifest,app,geoJsonFiles);
        },app);
        String module = app.substring(0, 1).toUpperCase() + app.substring(1)+".xml";
        Logger.gl().d("PARSE","App Name: "+module);
        Loader.getModule(moduleFile -> {
            if (moduleFile != null) {
                Logger.gl().d("LOAD", "[Bundle loaded.]");
                try {
                    Log.d("WORK",moduleFile.toString());
                    new WorkflowBundle().stringify(moduleFile).parse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, app, module);

        Loader.getModule(moduleFile -> {
            if (moduleFile != null) {
                Logger.gl().d("LOAD", "[Spinners loaded.]");
                try {
                    new Spinners().strip(moduleFile).parse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, app, "Spinners.csv");

        Loader.getModule(configF -> {
            if (configF != null) {
                Logger.gl().d("LOAD", "[Configs loaded.]");
                try {
                    Loader.getModule(variableF -> {
                        if (variableF != null) {
                            Logger.gl().d("LOAD", "[Variables loaded.]");
                            try {
                                new VariablesConfiguration().strip(variableF).parse(
                                        new GroupsConfiguration().strip(configF).parse()
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            Logger.gl().e("LOAD", "GroupsConfiguration missing");
                    }, app,"Variables.csv");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                Logger.gl().e("LOAD", "GroupsConfiguration missing");
        }, app, "Groups.csv");


    }

    public LiveData<List<VariableTable>> getAllVariables() { return mVariables; }
    public List<String> getManifest(){ return mManifest; }
    public void insert(VariableTable variable) { mRepository.insert(variable); }
    public LiveData<Logger> getLoadProgress() { if (mLoadLog == null) { mLoadLog = new MutableLiveData<Logger>(Logger.gl()); } return mLoadLog;}

}