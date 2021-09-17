package com.teraime.poppyfield.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.gis.GisType;
import com.teraime.poppyfield.room.FieldPadRepository;
import com.teraime.poppyfield.room.VariableTable;

import java.util.ArrayList;
import java.util.List;

public class WorldViewModel extends AndroidViewModel {

    private final FieldPadRepository mRepository;
    private final MutableLiveData<List<String>> mLoadLog = new MutableLiveData<>();
    private List<String> mManifest;
    private final LiveData<List<VariableTable>> mVariables;

    public WorldViewModel(Application application) {
        super(application);
        mRepository = new FieldPadRepository(application);
        mVariables = mRepository.getTimeOrderedList();
        mLoadLog.setValue(new ArrayList<>());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        //Load manifest
        String app=prefs.getString("App","smabio");
        List<List<String>> geoJsonFiles = new ArrayList<>();
        Loader.getManifest(fileList -> {
            mManifest = (List<String>)fileList;
            Loader.loadAllFiles(new LoaderCb() {
                @Override
                public void loaded(List<String> _d) {
                    int i=0;
                    List<GisType> gisTypeL = new ArrayList<>();
                    for (List<String>geoJ:geoJsonFiles) {
                        try {
                            long t1 = System.currentTimeMillis();
                            String type = mManifest.get(i++);
                            GisType gf = new GisType();
                            gisTypeL.add(gf.strip(geoJ).parse(type));
                            long diff = (System.currentTimeMillis()-t1);
                            mLoadLog.getValue().add("Parsed "+type+"("+gf.getVersion()+") in "+diff+" millsec");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("v","DELETE ALL CALLED");
                    mLoadLog.getValue().add("Deleting");
                    mRepository.deleteAll();
                    for (GisType gisType : gisTypeL) {
                        long t1 = System.currentTimeMillis();
                        List<GisObject> geo = gisType.getGeoObjects();
                        for (GisObject g:geo)
                            mRepository.insert(g);
                        long diff = (System.currentTimeMillis()-t1);
                        mLoadLog.getValue().add("Inserted "+geo.size()+" "+ gisType.getType()+" in "+diff+" millsec");

                        mLoadLog.postValue(mLoadLog.getValue());

                    }
                    mLoadLog.getValue().add("DONE.");

                }
            },mLoadLog,mManifest,app,geoJsonFiles);
        },app);
        Loader.getSpinners(spinnerF -> {
            mLoadLog.getValue().add("[Spinners loaded.]");
            try {
            Spinners sp = new Spinners().strip(spinnerF).parse();
                Log.d("SPIN",sp.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        },app);

    }

    public LiveData<List<VariableTable>> getAllVariables() { return mVariables; }
    public List<String> getManifest(){ return mManifest; }
    public void insert(VariableTable variable) { mRepository.insert(variable); }
    public LiveData<List<String>> getLoadProgress() { return mLoadLog; }

}