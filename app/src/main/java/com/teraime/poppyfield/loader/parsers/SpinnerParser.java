package com.teraime.poppyfield.loader.parsers;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.base.Tools;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinnerParser {
    private final static int noOfRequiredColumns=5;
    public static Map<String,List<Spinners.SpinnerElement>> parse(List<String> rows) throws ParseException {
        List<Spinners.SpinnerElement> sl=null;
        Map<String,List<Spinners.SpinnerElement>> sd=new HashMap<>();
        Logger.gl().d("PARSE","parsing Spinners. "+rows.size()+" lines");
        int c = 0;
        //remove header
        rows.remove(0);
        String curId = null;
        for (String row: rows) {
            String[] r = Tools.split(row);
            if (r.length < noOfRequiredColumns) {
                Logger.gl().e("Too short row in spinnerdef file. Row #" + c+1 + " has " + r.length + " columns but should have " + noOfRequiredColumns + " columns");
                for (int i = 0; i < r.length; i++)
                    Logger.gl().e( "R" + i + ":" + r[i]);
                throw new ParseException("Spinnerdef file corrupt.", -1);
            } else {
                String id = r[0];
                if (!id.equals(curId)) {
                    c = 0;
                    Logger.gl().d("PARSE","Adding new spinner list with ID " + id);
                    sl = new ArrayList<>();sd.put(id, sl);curId = id;
                }
                sl.add(new Spinners.SpinnerElement(r[1], r[2], r[3], r[4]));
                c++;
            }
        }
        return sd;
    }
}
