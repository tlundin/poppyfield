package com.teraime.poppyfield.base;

import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class Tools {

    public static Fragment createFragment(String templateName) throws ClassNotFoundException {
        Fragment f = null;
        try {
            Class<?> cs = Class.forName(Constants.JAVA_APP_NAME+".templates."+templateName);
            f = (Fragment)cs.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) { throw new ClassNotFoundException("Failed to create Fragment "+templateName); }
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
