package com.teraime.poppyfield.base;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.teraime.poppyfield.templates.GISPage;
import com.teraime.poppyfield.templates.LogScreen;
import com.teraime.poppyfield.templates.Page;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Tools {

    public static String readFromCache(Context mContext, String sFileName) throws IOException
        {
            byte[] encoded = Files.readAllBytes(Paths.get(mContext.getFilesDir().getPath(), "cache",sFileName));
            return new String(encoded, Charset.defaultCharset());
        }
    public static void writeToCache(Context mContext, String sFileName, List<String> arr){

        File dir = new File(mContext.getFilesDir(), "cache");
        File gpxFile = null;
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            gpxFile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxFile);
            for(String str: arr) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        Logger.gl().d("IO","New Cache entry: "+dir+"/"+sFileName);

    }

    public static Page createPage(WorldViewModel model, String template, Workflow wf) {
        switch (template) {
            case "GisMapTemplate":
                return new GISPage(model,template,wf);
            default:
                return new Page(model,template,wf);
        }
    }

    public static Fragment createFragment(String templateName)  {
        Fragment f=null;
        try {
            Class<?> cs = Class.forName(Constants.JAVA_APP_NAME+".templates."+templateName);
            f = (Fragment)cs.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) { Logger.gl().e("Failed to create Fragment "+templateName); }
        return f;
    }

    public static String[] split(String input) {
        List<String> result = new ArrayList<>();
        int start = 0;
        boolean inQuotes = false;
        for (int current = 0; current < input.length(); current++) {
            if (input.charAt(current) == '\"') inQuotes = !inQuotes; // toggle state
            boolean atLastChar = (current == input.length() - 1);
            if(atLastChar) {
                if (input.charAt(current) == ',') {
                    if (start==current)
                        result.add("");
                    else
                        result.add(input.substring(start,current));
                    result.add("");
                } else {
                    //Logger.gl().d("nils","Last char: "+input.charAt(current));
                    result.add(input.substring(start));
                }
            }
            else if (input.charAt(current) == ',' && !inQuotes) {
                String toAdd = input.substring(start, current);
                //Logger.gl().d("Adding",toAdd);

                result.add(toAdd);
                start = current + 1;
            }
        }
        if (result.size()==0)
            return new String[]{input};
        else
            return result.toArray(new String[0]);

    }


}
