package com.teraime.poppyfield.loader;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.base.Table;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Configurations.GisType;
import com.teraime.poppyfield.loader.Configurations.GroupsConfiguration;
import com.teraime.poppyfield.loader.Configurations.VariablesConfiguration;
import com.teraime.poppyfield.loader.Configurations.WorkflowBundle;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.ArrayList;
import java.util.List;

public class Loader {

    private static Loader instance=null;
    private final List<Config<?>> mConfigs = new ArrayList<>();
    private final MutableLiveData<String> logPing = new MutableLiveData<>();
    private WorkflowBundle wf;
    private Spinners spinners;
    private Table t;

    public static Loader getInstance() {
        if (instance == null)
            instance = new Loader();
        return instance;
    }

    public List<Config<?>> getConfigs() {
        return mConfigs;
    }

    public void load(String app, WorldViewModel v) {
        List<List<String>> geoJsonFiles = new ArrayList<>();

        WebLoader.getManifest(fileList -> {
            //List<String> mManifest = (List<String>) fileList;
            WebLoader.loadGisModules(_d -> {
                int i=0;
                List<GisType> gisTypeL = new ArrayList<>();
                for (List<String>geoJ:geoJsonFiles) {
                    try {
                        long t1 = System.currentTimeMillis();
                        String type = fileList.get(i++);
                        GisType gf = new GisType();
                        gisTypeL.add(gf.strip(geoJ).stringify().parse(type));
                        Tools.writeToCache(v.getApplication(),gf.getType(),gf.getRawData());
                        long diff = (System.currentTimeMillis()-t1);
                        Logger.gl().d("PARSE","Parsed "+type+"("+gf.getVersion()+") in "+diff+" millsec");
                        logPing.setValue("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Logger.gl().d("INSERT","DELETE ALL CALLED");
                v.deleteAllGisObjects();
                for (GisType gisType : gisTypeL) {
                    long t1 = System.currentTimeMillis();
                    List<GisObject> geo = gisType.getGeoObjects();
                    for (GisObject g:geo)
                        v.insertGisObject(g);
                    long diff = (System.currentTimeMillis()-t1);
                    Logger.gl().d("INSERT","Inserted "+geo.size()+" "+ gisType.getType()+" in "+diff+" millsec");

                }
                Logger.gl().d("INSERT","DONE.");
                logPing.setValue("");

            },fileList,app,geoJsonFiles);
        },app);
        String module = app.substring(0, 1).toUpperCase() + app.substring(1)+".xml";
        Logger.gl().d("PARSE","App Name: "+module);
        WebLoader.getModule(moduleFile -> {
            if (moduleFile != null) {
                Logger.gl().d("LOAD", "[Bundle loaded.]");
                logPing.setValue("");
                try {
                    Log.d("WORK",moduleFile.toString());
                    wf = new WorkflowBundle().stringify(moduleFile).parse();
                    mConfigs.add(wf);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, app, module);

        WebLoader.getModule(moduleFile -> {
            if (moduleFile != null) {
                Logger.gl().d("LOAD", "[Spinners loaded.]");
                logPing.setValue("");
                try {
                    spinners = new Spinners().strip(moduleFile).parse();
                    mConfigs.add(spinners);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, app, "Spinners.csv");

        WebLoader.getModule(configF -> {
            if (configF != null) {
                Logger.gl().d("LOAD", "[Configs loaded.]");

                try {
                    WebLoader.getModule(variableF -> {
                        if (variableF != null) {
                            Logger.gl().d("LOAD", "[Variables loaded.]");
                            logPing.setValue("");
                            try {
                                GroupsConfiguration gc = new GroupsConfiguration().strip(configF).parse();
                                VariablesConfiguration vc = new VariablesConfiguration().strip(variableF).parse(gc);
                                mConfigs.add(gc);
                                mConfigs.add(vc);
                                t = vc.getTable();
                                ((MutableLiveData)v.getMyConf()).setValue(mConfigs);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            Logger.gl().e("GroupsConfiguration missing");
                    }, app,"Variables.csv");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                Logger.gl().e("GroupsConfiguration missing");
        }, app, "Groups.csv");
    }

    public WorkflowBundle getBundle() {
        return wf;
    }
    public Spinners getSpinners() {
        return spinners;
    }
    public Table getTable() {
        return t;
    }
    public MutableLiveData<String> getLogObservable() { return logPing;   }
}
