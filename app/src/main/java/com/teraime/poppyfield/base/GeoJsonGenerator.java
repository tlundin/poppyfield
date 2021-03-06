package com.teraime.poppyfield.base;

import android.util.JsonWriter;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.teraime.poppyfield.gis.Geomatte;
import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.room.VariableTable;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class GeoJsonGenerator {

    static StringWriter sw;
    static JsonWriter writer;

    public static JSONObject print(List<VariableTable> inList, DBHelper.ColTranslate colTranslator) {

        Map<String,Map<String,VariableTable>> uuidMap = new HashMap<>();

        inList.forEach((item) -> {
            String uuid = item.getUUID();
            Map<String,VariableTable> lt = uuidMap.get(uuid);
            if (lt == null) {
                Log.d("GEOGEN","adding "+uuid);
                lt = new HashMap<>();
                uuidMap.put(uuid,lt);
            }
            lt.put(item.getVar(),item);
        });

        try {


            sw = new StringWriter();
            writer = new JsonWriter(sw);
            writer.setIndent("  ");
            //Begin main obj
            writer.beginObject();
            //Log.d("nils","Writing header");
            write("name","Export");
            write("type","FeatureCollection");
            writer.name("crs");
            writer.beginObject();
            write("type","name");
            writer.name("properties");
            writer.beginObject();
            write("name","EPSG:3006");
            writer.endObject();
            //end header
            writer.endObject();
            writer.name("features");
            writer.beginArray();
            for (String uuid:uuidMap.keySet()) {
                Map<String,VariableTable> props = uuidMap.get(uuid);
                writer.beginObject();
                write("type", "Feature");
                writer.name("geometry");
                writer.beginObject();
                VariableTable geoTypeVT = props.remove("geotype");
                String geoType = geoTypeVT.getValue();
                write("type", geoType);
                writer.name("coordinates");
                boolean isPoly= "Polygon".equalsIgnoreCase(geoType);
                boolean isLineString = "Linestring".equalsIgnoreCase(geoType);
                String coordinates = props.remove(GisConstants.GPS_Coord_Var_Name).getValue();
                String[] polygons = coordinates.split("\\|");

                if (isPoly||isLineString)
                    writer.beginArray();

                for (String polygon : polygons) {
                    String[] coords = polygon.split(",");
                    if (isPoly)
                        writer.beginArray();
                    //                   Log.d("BLAHA", "is poly? " + isPoly);
                    //                   Log.d("BLAHA", "geotype is  " + geoType);
                    //                   Log.d("BLAHA", "Length is " + coords.length);

                    for (int i = 0; i < coords.length; i += 2) {
                        //                       Log.d("vortex", "coord [" + i + "] :" + coords[i] + " [" + (i + 1) + "] :" + coords[i + 1]);
                        writer.beginArray();
                        LatLng ll = Geomatte.convertToLatLong(Double.parseDouble(coords[i]), Double.parseDouble(coords[i+1]));
                        printCoord(writer,ll.longitude+"");
                        printCoord(writer, ll.latitude+"");
                        writer.endArray();
                    }
                    if (isPoly)
                        writer.endArray();
                }
                if (isPoly||isLineString)
                    writer.endArray();
                writer.endObject();
                writer.name("properties");
                writer.beginObject();
                writer.name("VARIABLES");
                writer.beginObject();
                for (String key:props.keySet())
                    write(props.get(key).getVar(),props.get(key).getValue());
                writer.endObject();
                writer.name("COLUMNS");
                writer.beginObject();
                write(GisConstants.FixedGid, uuid);
                write("UID", uuid);
                //Log.d("",colTranslator.ToDB("trakt"));
                //String trakt = geoTypeVT.getCol(colTranslator.ToDB(NamedVariables.AreaTerm));
                //write(NamedVariables.AreaTerm.toUpperCase(),trakt);
                write("AUTHOR", geoTypeVT.getAuthor());
                write("TEAM", geoTypeVT.getLag());
                write("??R", geoTypeVT.getYear());
                write(colTranslator.ToReal("L1"),geoTypeVT.getL1());
                write(colTranslator.ToReal("L2"),geoTypeVT.getL2());
                write(colTranslator.ToReal("L3"),geoTypeVT.getL3());
                write(colTranslator.ToReal("L4"),geoTypeVT.getL4());
                write(colTranslator.ToReal("L5"),geoTypeVT.getL5());
                write(colTranslator.ToReal("L6"),geoTypeVT.getL6());
                write(colTranslator.ToReal("L7"),geoTypeVT.getL7());
                write(colTranslator.ToReal("L8"),geoTypeVT.getL8());
                write(colTranslator.ToReal("L9"),geoTypeVT.getL9());
                write(colTranslator.ToReal("L10"),geoTypeVT.getL10());
                writer.endObject();
                writer.endObject();
                writer.endObject();
                //    Log.d("BLAHA", "gistyp is  " + geoTypeVT.getCol(colTranslator.ToDB("gistyp")));
                //    Log.d("BLAHA", "trakt  is  " + geoTypeVT.getCol(colTranslator.ToDB("trakt")));
            }
            //End of array.
            writer.endArray();
            //End of all.
            writer.endObject();
            Log.d("GEOGEN",sw.toString());
            return new JSONObject(sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void write(String name,String value) throws IOException {
        String val = (name==null||value==null||value.length()==0)?"NULL":value;
        if (!val.equals("NULL"))
            writer.name(name).value(val);
    }

    private static void printCoord(JsonWriter writer, String coord) {
        try {
            if (coord == null || "null".equalsIgnoreCase(coord)) {
                Log.e("vortex", "coordinate was null in db. ");

                writer.nullValue();

            } else {
                try {
                    writer.value(Float.parseFloat(coord));
                } catch (NumberFormatException e) {
                    writer.nullValue();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();

        }
    }

}
