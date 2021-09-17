package com.teraime.poppyfield.parsers;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.util.MalformedJsonException;
import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.gis.Location;
import com.teraime.poppyfield.gis.GisPolygonObject;
import com.teraime.poppyfield.gis.SweLocation;
import com.teraime.poppyfield.loader.NamedVariables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GeoJsonParser {
    public static List<GisObject> parse(JsonReader reader,String type) throws IOException {
        final List<GisObject> myGisObjects = new ArrayList<>();

        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("features")) {
                    reader.beginArray();
                    break;
                } else
                    reader.skipValue();
            }
                while (reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                    reader.beginObject();
                    Map<String, String> keyChain = new HashMap<>();
                    Map<String, String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    //feature or geometry in any order.
                    boolean featureF = false, geometryF = false, propF = false;
                    int attrCount1 = 0;
                    while (!(geometryF && featureF && propF)) {
                        //avoid spiraling forever.
                        attrCount1++;
                        if (attrCount1 > 3) {
                            if (!geometryF) {
                                throw new MalformedJsonException("Attribute Geometry missing in Json file");
                            }
                            break;
                        }
                        String nName = reader.nextName();
                        switch (nName) {
                            case "type":
                                //This type attribute is discarded
                                getAttribute(reader);
                                featureF = true;
                                break;
                            case "properties":
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    if (reader.peek() != JsonToken.NUMBER) {
                                        String name = reader.nextName().toLowerCase();
                                        attributes.put(name, getAttribute(reader));
                                    }
                                }
                                //end attributes
                                reader.endObject();
                                String uuid = attributes.remove(GisConstants.FixedGid);
                                String rutaId = attributes.remove(GisConstants.RutaID);
                                if (uuid != null) {
                                    uuid = uuid.replace("{", "").replace("}", "");
                                    keyChain.put("UUID", uuid);
                                } else
                                    throw new MalformedJsonException("missing 'FIXEDGID', cannot continue");

                                if (rutaId != null)
                                    keyChain.put(NamedVariables.AreaTerm, rutaId);
                                //Add geotype to attributes so that the correct object can be used at export.
                                keyChain.put(GisConstants.TYPE_COLUMN, type);
                                attributes.put(GisConstants.Geo_Type, type);
                                propF = true;
                                break;
                            case "geometry":
                                geometryF = true;
                                //Coordinates.
                                reader.beginObject();
                                //Next can be either coordinates or geo type.
                                int attrCount2 = 0;
                                boolean geoTypeF = false, coordinatesF = false;
                                List<Location> myCoordinates = null;
                                Map<String, List<Location>> polygonSet = new HashMap<>();
                                while (!(geoTypeF && coordinatesF)) {
                                    attrCount2++;
                                    if (attrCount2 > 2)
                                        throw new MalformedJsonException("Attribute " + (geoTypeF ? "Coordinates" : "GeoType") + " missing in Json file");
                                    nName = reader.nextName();
                                    switch (nName) {
                                        case "type":
                                            //This is the geotype, eg. "polygon"
                                            type = getAttribute(reader);
                                            geoTypeF = true;
                                            if (type == null)
                                                throw new MalformedJsonException("Type field expected (point, polygon..., but got null");
                                            type = type.trim();
                                            break;
                                        case "coordinates":
                                            coordinatesF = true;
                                            //Always start with an array [
                                            reader.beginArray();
                                            //If single point, next must be number.
                                            if (reader.peek() == JsonToken.NUMBER) {
                                                myGisObjects.add(new GisObject(keyChain, Collections.singletonList(readLocation(reader)), attributes));
                                            } else {
                                                //next must be an array. Otherwise error.
                                                //[->[
                                                //If multipoint or Linestring, next is a number.
                                                //[[->1,2,3],[...]]
                                                reader.beginArray();
                                                if (reader.peek() == JsonToken.NUMBER) {
                                                    myCoordinates = (readAllLocations(reader));
                                                } else {
                                                    int id = 1;
                                                    reader.beginArray();
                                                    //If polygon next is a number.
                                                    //"coordinates": [
                                                    //[ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],
                                                    //[100.0, 1.0], [100.0, 0.0] ]
                                                    //]
                                                    if (reader.peek() == JsonToken.NUMBER) {
                                                        boolean stillMore = true;
                                                        while (stillMore) {
                                                            polygonSet.put(id + "", readAllLocations(reader));
                                                            id++;
                                                            reader.endArray();
                                                            if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                                                                stillMore = false;
                                                            } else {
                                                                //found another poly
                                                                reader.beginArray();
                                                            }
                                                        }
                                                    } else {
                                                        //Multipolygon - Array of polygon arrays.
                                                        reader.beginArray();
                                                        if (reader.peek() == JsonToken.NUMBER) {
                                                            boolean stillMore = true;
                                                            while (stillMore) {
                                                                polygonSet.put(id + "", readAllLocations(reader));
                                                                id++;
                                                                //end this poly..look if more.
                                                                while (reader.peek() == JsonToken.END_ARRAY)
                                                                    reader.endArray();
                                                                if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                                                                    stillMore = false;
                                                                } else {
                                                                    reader.beginArray();
                                                                    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                                                        //found another multi
                                                                        reader.beginArray();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //Always end with endarrays.
                                            while (reader.peek() == JsonToken.END_ARRAY)
                                                reader.endArray();
                                            break;
                                        default:
                                            Log.e("vortex", "in default...not good: " + reader.peek() + "::::" + reader.toString());
                                            List<String> skippies = new ArrayList<>();
                                            while (reader.hasNext()) {
                                                String skipped = getAttribute(reader);
                                                if (skipped.length() > 0)
                                                    skippies.add(skipped);
                                            }
                                            if (skippies.size() > 0) {
                                                Log.e("vortex", "Skipped " + skippies.size() + " attributes for " + type + ":");
                                                for (String skip : skippies)
                                                    Log.e("vortex", skip);
                                            }
                                            break;
                                    }
                                }
                                //now we have type and coordinates.
                                switch (type) {
                                    case GisConstants.LINE_STRING:
                                    case GisConstants.MULTI_POINT:
                                        if (myCoordinates != null && !myCoordinates.isEmpty())
                                            myGisObjects.add(new GisObject(keyChain, myCoordinates, attributes));
                                        else
                                            Log.e("vortex", "No coordinates for multipoint in " + type + "!");
                                        break;

                                    case GisConstants.POLYGON:
                                    case GisConstants.MULTI_POLYGON:
                                        myGisObjects.add(new GisPolygonObject(keyChain, polygonSet, attributes));
                                        break;
                                }
                                reader.endObject();
                                break;
                        }
                    }
                    //end row
                    reader.endObject();
                } //else
                   // throw new MalformedJsonException("Parse error when parsing geojson type " + type + ". Expected Object type at " + reader.toString() + " peek: " + reader.peek());

            } catch(MalformedJsonException je){
                Log.e("vortex", je.getMessage());
            }

        reader.close();
        return myGisObjects;
    }

    private static String getAttribute(JsonReader reader) throws IOException {
        String ret=null;
        if (reader.peek() != JsonToken.NULL) {
            if (reader.peek() == JsonToken.STRING) {
                ret = reader.nextString();
                if (ret.isEmpty())
                    ret = null;
            } else if (reader.peek() == JsonToken.NUMBER)
                ret = reader.nextString();
            else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.peek() != JsonToken.END_OBJECT) {
                    String name = reader.nextName();
                    String attr = getAttribute(reader);
                    ret = name+":"+attr;
                }
                reader.endObject();
            }
        }
        else
            reader.nextNull();
        return ret;
    }

    private static List<Location> readAllLocations(JsonReader reader) {
        List<Location> myLocation = new ArrayList<>();
        try {
            while (!reader.peek().equals(JsonToken.END_ARRAY)) {
                if (reader.peek().equals(JsonToken.BEGIN_ARRAY))
                    reader.beginArray();
                myLocation.add(readLocation(reader));
                reader.endArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myLocation;
    }

    private static Location readLocation(JsonReader reader) {
        double x = 0;
        double y = 0;
        try {
            x = reader.nextDouble();
            y = reader.nextDouble();
            if (!reader.peek().equals(JsonToken.END_ARRAY)) {
                //skip z value
                reader.nextDouble();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SweLocation(x,y);
    }
}



