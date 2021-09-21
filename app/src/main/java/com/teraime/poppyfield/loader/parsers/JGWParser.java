package com.teraime.poppyfield.loader.parsers;

import android.util.Log;

import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.gis.PhotoMeta;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class JGWParser {
    private final static String[] pars = new String[6];
    public static PhotoMeta parse(List<String> rows, double Width,double Height) throws ParseException {
        int currentRow=0;
        for (String row:rows) {
            Log.d("jgw", "Row: " + row);
            if (currentRow < pars.length)
                pars[currentRow++] = row;
            else
                throw new ParseException("JGW Parser: Too many rows in JGW file", currentRow);
        }
        try {
            //pars[n] now contains row n in jgq file.
            double XCellSize = Double.parseDouble(pars[0]);
            //dont care about rotation in row 1 and row 2.
            double YCellSize = Double.parseDouble(pars[3]);
            double WorldX = Double.parseDouble(pars[4]);
            double WorldY = Double.parseDouble(pars[5]);

            double W = WorldX - (XCellSize / 2);
            double N = WorldY - (YCellSize / 2);
            double E = (WorldX + (Width * XCellSize)) - (XCellSize / 2);
            double S = (WorldY + (Height * YCellSize)) - (YCellSize / 2);

            PhotoMeta p = new PhotoMeta(N, E, S, W);
            Log.d("jgw","N: E: S: W: "+N+","+E+","+","+S+","+W);
            return p;
        }
        catch(NumberFormatException ex) {
            Log.e("jgw","Photometa file is corrupt");
            throw new ParseException("Corrupt JGW file", currentRow);
        }

    }
}
