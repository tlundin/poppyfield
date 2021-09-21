package com.teraime.poppyfield.loader;

import android.os.AsyncTask;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.S;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class WebLoader {

    static String protocol = "http://";

    
    public static void getManifest(LoaderCb callback, String app) {
        String Manifest = "content.txt";
        String url = S.SERVER + "/" + app + "/gis_objects/";
        new DownloadFileTask(callback).execute(protocol+ url + Manifest);
    }
    public static void getModule(LoaderCb callback, String app, String module) {
        String url = S.SERVER + "/" + app + "/";
        new DownloadFileTask(callback).execute(protocol+ url + module);
    }

    public static void loadGisModules(LoaderCb loaderCb, List<String> mManifest, String app, List<List<String>> files) {
        String url = "www.teraim.com/"+app+"/gis_objects/";
        for (String gisFile:mManifest) {
            new DownloadFileTask(file -> {
                Logger.gl().d("LOAD",gisFile);
                files.add(file);
                if (files.size()==mManifest.size())
                    loaderCb.loaded(null);
            }).execute(protocol+ url + gisFile + ".json");
        }
    }

    public static void getMapMetaData(LoaderCb callback, String app, String picName) {
        String metaFile = picName.replace("jpg","jgw");
        String url = S.SERVER + "/" + app + "/extras/";
        new DownloadFileTask(callback).execute(protocol+ url + metaFile);
    }
    
    private static class DownloadFileTask extends AsyncTask<String, Void, List<String>> {
        final LoaderCb cb;

        DownloadFileTask(LoaderCb cb) {
            this.cb=cb;
        }

        protected List<String> doInBackground(String... url) {
            String inputLine;
            URL website=null;
            BufferedReader in;
            List<String> file = null;
            try {
                website = new URL(url[0]);
                Logger.gl().d("URL",website.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                assert website != null;
                URLConnection ucon = website.openConnection();
                ucon.setConnectTimeout(5000);
                in = new BufferedReader(new InputStreamReader(ucon.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    if (file==null)
                        file = new ArrayList<>();
                    file.add(inputLine);
                }
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
                file = null;
            }
            return file;
        }

        protected void onPostExecute(List<String> file) {
            cb.loaded(file);
        }


    }
}
