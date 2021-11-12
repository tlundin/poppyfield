package com.teraime.poppyfield.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.pages.GISPage;
import com.teraime.poppyfield.pages.Page;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Tools {

    public static boolean isNumeric(Object num)
    {
        //Log.d("vortex","isnumeric "+num);
        if (num == null)
            return false;
        if (num instanceof Double || num instanceof Float || num instanceof Integer)
            return true;
        if (num instanceof String) {
            String str = (String)num;
            if (str==null||str.length()==0)
                return false;
            int i=0;
            //Log.d("vortex","isnumeric? str "+str);
            for (char c : str.toCharArray())
            {

                if (!Character.isDigit(c)&& c!='.' && c!='E' && c!='-') {
                    return false;
                }
                i++;
            }
            //Log.d("vortex","isnumeric yes");
            return true;
        } else {
            System.out.println("isNumeric returns false...not a string: "+num.getClass()+" "+num);
            return false;
        }
    }


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

    public static void writeImageToCache(File cacheFolder, String picName, Bitmap bmp) {
        File file = new File(cacheFolder, picName);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean imageIsCached(File cacheFolder,String picName) {
        File file = new File(cacheFolder, picName);
        return file.exists();
    }



    public static Page createPage(WorldViewModel model, String template, Workflow wf, String name) {
        switch (template) {
            case "GisMapTemplate":
                return new GISPage(model,template,wf,name);
            default:
                return new Page(model,template,wf,name);
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


    //Extract the name value pair of a given variable entry.
    public static Map<String,String> extractValues(List<VariableTable> variableTables) {
        Map<String,String> ret = new HashMap<>();
        for (VariableTable vt:variableTables) {
            ret.put(vt.getVar(),vt.getValue());
        }
        return ret;
    }

    public static Map<String,String> extractColumns(VariableTable vt) {
        return vt.toMap();
    }


    public static Map<String, String> jsonObjectToMap(String json) {
        try {
            Map<String, String> ret = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            JSONObject jo = new JSONObject(json);
            for (Iterator<String> it = jo.keys(); it.hasNext(); ) {
                String key = it.next();
                ret.put(key, jo.getString(key));

            }
            return ret;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getColorResource(Context ctx, String colorName) {
        return Tools.getColorResource(ctx,colorName, R.color.black);
    }

    public static int getColorResource(Context ctx, String colorName, int defaultColor) {

        if(colorName !=null) {
            if (colorName.startsWith("#"))
                return Color.parseColor(colorName);
            else if (colorName.equalsIgnoreCase("black"))
                return Color.BLACK;
            else if (colorName.equalsIgnoreCase("white"))
                return Color.WHITE;
            else if (colorName.equalsIgnoreCase("green"))
                return Color.GREEN;
            else if (colorName.equalsIgnoreCase("red"))
                return Color.RED;
            else if (colorName.equalsIgnoreCase("blue"))
                return Color.BLUE;
            else if (colorName.equalsIgnoreCase("Lightgray"))
                return Color.parseColor("#D3D3D3");
            try {
                int resourceId = ctx.getResources().getIdentifier(colorName.toLowerCase(), "color", ctx.getPackageName());
                return ctx.getColor(resourceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("plax","Color "+colorName+" not known...returning default");
        return ctx.getColor(defaultColor);
    }


    public static LiveData<List<VariableTable>> requestDynamicList(Variable variable) {
        String[] opt = null;
        WorldViewModel model = WorldViewModel.getStaticWorldRef();
        Variable.VariableConfiguration al = variable.getVariableConfiguration();
        com.teraime.poppyfield.base.Context.VariableCache vc = variable.getContext().getVariableCache();

        Logger o = Logger.gl();
        List<String> listValues = al.getListElements();
        Log.d("nils", "Found dynamic list definition for variable " + variable.getId() + ": " + listValues);

        if (listValues != null && listValues.size() > 0) {
            String[] columnSelector = listValues.get(0).split("=");
            String[] column = null;
            boolean error = false;
            if (columnSelector[0].equalsIgnoreCase("@col")) {
                Log.d("nils", "found column selector");
                //Column to select.
                String dbColName = model.getDatabaseColumnName(columnSelector[1]);
                if (dbColName != null) {
                    Log.d("nils", "Real Column name for " + columnSelector[1] + " is " + dbColName);
                    column = new String[1];
                    column[0] = dbColName;
                } else {
                    Log.d("nils", "Column referenced in List definition for variable " + al.getVarLabel() + " not found: " + columnSelector[1]);
                    o.e("Column referenced in List definition for variable " + al.getVarLabel() + " not found: " + columnSelector[1]);
                    error = true;
                }
                if (!error) {
                    //Any other columns part of key?
                    Map<String, String> keySet = new HashMap<String, String>();
                    if (listValues.size() > 1) {
                        //yes..include these in search
                        Log.d("nils", "found additional keys...");
                        String[] keyPair;
                        for (int i = 1; i < listValues.size(); i++) {
                            keyPair = listValues.get(i).split("=");
                            if (keyPair != null && keyPair.length == 2) {
                                String valx = variable.getContext().getVariableValues().get(keyPair[1]);
                                if (valx != null)
                                    keySet.put(keyPair[0], valx);
                                else {
                                    Log.e("nils", "The variable " + keyPair[1] + " used for dynamic list " + variable.getLabel() + " is not returning a value");
                                    o.e("The variable " + keyPair[1] + " used for dynamic list " + variable.getLabel() + " is not returning a value");
                                }
                            } else {
                                Log.d("nils", "Keypair error: " + Arrays.toString(keyPair));
                                o.e("Keypair referenced in List definition for variable " + variable.getLabel() + " cannot be read: " + Arrays.toString(keyPair));
                            }
                        }

                    } else
                        Log.d("nils", "no additional keys..only column");


                    LiveData<List<VariableTable>> values = model.queryFromMap(keySet);
                    return values;

                }
            }
        }
        return null;
    }
}
