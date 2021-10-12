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

    private final List<Config<?>> mConfigs = new ArrayList<>();
    private final List<GisType> geoDataConfigs = new ArrayList<>();
    private final MutableLiveData<String> logPing = new MutableLiveData<>();
    private WorkflowBundle wf;
    private Spinners spinners;
    private Table t;
    private WorldViewModel mWorld;
    private String mApp;


    public List<Config<?>> getConfigs() {
        return mConfigs;
    }

    /** Async Load of MetaData
     *
     * @param app - Name of Application
     * @param v - The View Model
     */
    public void load(String app, WorldViewModel v) {
        mWorld = v;mApp = app;

        ////////////////
        loadGisObjects();
        loadWorkFlows();
        loadSpinners();
        loadTableData();
        ////////////////


    }

    private void loadGisObjects() {

        List<List<String>> geoJsonFiles = new ArrayList<>();
        WebLoader.getManifest(fileList -> {
            //List<String> mManifest = (List<String>) fileList;
            WebLoader.loadGisModules(_d -> {
                int i = 0;

                for (List<String> geoJ : geoJsonFiles) {
                    try {
                        long t1 = System.currentTimeMillis();
                        String type = fileList.get(i++);
                        GisType gf = new GisType();
                        geoDataConfigs.add(gf.strip(geoJ).stringify().parse(type));
                        Tools.writeToCache(mWorld.getApplication(), gf.getType(), gf.getRawData());
                        Log.d("WROOM", gf.getRawData().toString());
                        long diff = (System.currentTimeMillis() - t1);
                        Logger.gl().d("PARSE", "Parsed " + type + "(" + gf.getVersion() + ") in " + diff + " millsec");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Logger.gl().d("INSERT", "DELETE ALL CALLED");
                logPing.setValue("GisObjects");
            }, fileList, mApp, geoJsonFiles);
        }, mApp);

    }

    private void loadWorkFlows() {
        String module = mApp.substring(0, 1).toUpperCase() + mApp.substring(1) + ".xml";
        Logger.gl().d("PARSE", "App Name: " + module);
        WebLoader.getModule(moduleFile -> {
            if (moduleFile != null) {
                Logger.gl().d("LOAD", "[Bundle loaded.]");
                try {
                    Log.d("WORK", moduleFile.toString());
                    wf = new WorkflowBundle().stringify(moduleFile).parse();
                    mConfigs.add(wf);
                    logPing.setValue("Workflows");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, mApp, module);
    }

    private void loadSpinners() {
        WebLoader.getModule(moduleFile -> {
            if (moduleFile != null) {
                Logger.gl().d("LOAD", "[Spinners loaded.]");
                try {
                    spinners = new Spinners().strip(moduleFile).parse();
                    mConfigs.add(spinners);
                    logPing.setValue("Spinners");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, mApp, "Spinners.csv");
    }

    private void loadTableData() {
        WebLoader.getModule(configF -> {
            if (configF != null) {
                Logger.gl().d("LOAD", "[Configs loaded.]");

                try {
                    WebLoader.getModule(variableF -> {
                        if (variableF != null) {
                            Logger.gl().d("LOAD", "[Variables loaded.]");

                            try {
                                GroupsConfiguration gc = new GroupsConfiguration().strip(configF).parse();
                                VariablesConfiguration vc = new VariablesConfiguration().strip(variableF).parse(gc);
                                mConfigs.add(gc);
                                mConfigs.add(vc);
                                t = vc.getTable();
                                t.printTable();
                                ((MutableLiveData)mWorld.getMyConf()).setValue(mConfigs);
                                logPing.setValue("Table");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            Logger.gl().e("GroupsConfiguration missing");
                    }, mApp,"Variables.csv");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                Logger.gl().e("GroupsConfiguration missing");
        }, mApp, "Groups.csv");
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
    public List<GisType> getGeoData() {
        return geoDataConfigs;
    }
    public MutableLiveData<String> getLogObservable() { return logPing;   }
}
