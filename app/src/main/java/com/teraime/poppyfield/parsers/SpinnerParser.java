package com.teraime.poppyfield.parsers;
import android.util.Log;
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
        int c = 0;
        String header = rows.remove(0);
        Log.d("vortex", header);
        String curId = null;
        for (String row: rows) {
            //Split into lines.
            String[] r = Tools.split(row);
            if (r.length < noOfRequiredColumns) {
                Log.e("SpinnerParser", "Too short row in spinnerdef file. Row #" + c+1 + " has " + r.length + " columns but should have " + noOfRequiredColumns + " columns");
                for (int i = 0; i < r.length; i++) {
                    Log.e("SpinnerParser", "R" + i + ":" + r[i]);
                }
                throw new ParseException("Spinnerdef file corrupt.", -1);
            } else {
                String id = r[0];
                if (!id.equals(curId)) {
                    c = 0;
                    Log.d("vortex","Adding new spinner list with ID " + curId);
                    sl = new ArrayList<>();
                    sd.put(id, sl);
                    curId = id;

                }
                Log.d("vortex", "Added new spinner element. ID " + curId);
                sl.add(new Spinners.SpinnerElement(r[1], r[2], r[3], r[4]));
                c++;
            }
        }
        return sd;
    }
}
