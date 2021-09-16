package com.teraime.poppyfield.loader;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.SyncStateContract;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.base.S;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Loader  {

    static String protocol = "http://";

    public static void getManifest(LoaderCb callback, String app) {
        String Manifest = "content.txt";
        String url = S.SERVER + "/" + app + "/gis_objects/";
        new DownloadFileTask(callback).execute(protocol+ url + Manifest);
    }
    public static void getSpinners(LoaderCb callback, String app) {
        String spinners = "Spinners.csv";
        String url = S.SERVER + "/" + app + "/";
        new DownloadFileTask(callback).execute(protocol+ url + spinners);
    }


    public static void loadAllFiles(LoaderCb loaderCb, MutableLiveData<List<String>> mLoadLog, List<String> mManifest, String app, List<List<String>> files) {
        String url = "www.teraim.com/"+app+"/gis_objects/";
        for (String gisFile:mManifest) {
            new DownloadFileTask(new LoaderCb() {
                @Override
                public void loaded(List<String> file) {
                    mLoadLog.getValue().add(gisFile);
                    mLoadLog.postValue(mLoadLog.getValue());
                    files.add(file);
                    if (files.size()==mManifest.size())
                        loaderCb.loaded(null);
                }
            }).execute(protocol+ url + gisFile + ".json");
        }
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
                //Log.d("URL",website.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
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
            }
            return file;
        }

        protected void onPostExecute(List<String> file) {
            cb.loaded(file);
        }


    }
}
