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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public class Loader {

    private final List<Config<?>> mConfigs = new ArrayList<>();
    private final List<GisType> geoDataConfigs = new ArrayList<>();
    private final MutableLiveData<String> logPing = new MutableLiveData<>();
    private final Map<String,List<GisObject>> geoConfigMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
                loadManifest();
                loadGisObjects();
                loadWorkFlows();
                loadSpinners();
                loadTableData();
                ////////////////

    }

    private enum Mode {CORE,GIS,EXTRA};

    private void loadManifest() {
        AtomicReference<Mode> m= new AtomicReference<>(Mode.CORE);
        mWorld.setModuleCount(15);
        WebLoader.getManifest(fileList -> {
            if (fileList !=null) {
                Log.d("MANIFEST", fileList.toString());
                Set<String> headers = new HashSet<String>(Arrays.asList("core", "gis_objects", "extras"));
                int i = 0;
                String[] nameVer;
                if (fileList.containsAll(headers)) {
                    for (String configFile : fileList) {
                        if (headers.contains(configFile)) {
                            switch (configFile) {
                                case "core":
                                    m.set(Mode.CORE);
                                    continue;
                                case "gis_objects":
                                    m.set(Mode.GIS);
                                    continue;
                                default:
                                    m.set(Mode.EXTRA);
                                    continue;
                            }
                        }
                        Log.d("M", configFile);
                        nameVer = configFile.split(",");
                        if (nameVer.length == 2)
                            i++;
                    }
                    mWorld.setModuleCount(i-headers.size());
                } else
                    Logger.gl().e("incomplete manifest");
            }

        }, mApp);

    }

    private void loadGisObjects() {
        List<List<String>> geoJsonFiles = new ArrayList<>();
        WebLoader.getGisManifest(fileList -> {
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
                        geoConfigMap.put(type,gf.getGeoObjects());
                        logPing.postValue(type);
                        Log.d("WROOM", gf.getRawData().toString());
                        long diff = (System.currentTimeMillis() - t1);
                        Logger.gl().d("PARSE", "Parsed " + type + "(" + gf.getVersion() + ") in " + diff + " millsec");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Logger.gl().d("INSERT", "DELETE ALL CALLED");
                logPing.postValue("GisObjects");
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
                    logPing.postValue("Workflows");
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
                    logPing.postValue("Spinners");
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
                        mWorld.getExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                if (variableF != null) {
                                    Logger.gl().d("LOAD", "[Variables loaded.]");

                                    try {

                                        GroupsConfiguration gc = new GroupsConfiguration().strip(configF).parse();
                                        VariablesConfiguration vc = new VariablesConfiguration().strip(variableF).parse(gc);
                                        mConfigs.add(gc);
                                        mConfigs.add(vc);
                                        t = vc.getTable();
                                        //t.printTable();
                                        ((MutableLiveData)mWorld.getMyConf()).postValue(mConfigs);
                                        logPing.postValue("Table");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    Logger.gl().e("GroupsConfiguration missing");

                            }
                        });
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

    public List<GisObject> getGeoDataType(String type) {
        return geoConfigMap.get(type);
    }


}
