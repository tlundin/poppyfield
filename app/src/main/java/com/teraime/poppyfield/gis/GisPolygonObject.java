package com.teraime.poppyfield.gis;

import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.gis.Location;

import java.util.List;
import java.util.Map;

public class GisPolygonObject extends GisObject {
    private final Map<String, List<Location>> polygonSet;

    public GisPolygonObject(Map<String, String> keyChain, Map<String, List<Location>> polygonSet, Map<String, String> attributes) {
        super(keyChain,null,attributes);
        this.polygonSet = polygonSet;
    }

    @Override
    public String coordsToString() {
        if (polygonSet==null)
            return null;
        String ret="";
        for(List<Location> l:polygonSet.values()) {
            myCoordinates = l;
            ret+= super.coordsToString()+"|";
        }
        if (!ret.isEmpty())
            ret = ret.substring(0,ret.length()-1);
        return ret;
    }
}
