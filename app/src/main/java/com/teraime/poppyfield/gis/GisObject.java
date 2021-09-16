package com.teraime.poppyfield.gis;

import java.util.List;
import java.util.Map;

public class GisObject {
    private final Map<String, String> keyChain;
    protected List<Location> myCoordinates;
    private final Map<String, String> attributes;

    public GisObject(Map<String, String> keyChain, List<Location> myCoordinates, Map<String, String> attributes) {
        this.keyChain = keyChain;
        this.myCoordinates = myCoordinates;
        this.attributes = attributes;
    }

    public Map<String, String> getKeys() {
        return keyChain;
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String coordsToString() {
        if (myCoordinates == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (Location l:myCoordinates) {

            sb.append(l.toString());
            sb.append(",");
        }
        if (sb.length()>0)
            return sb.substring(0, sb.length()-1);
        else
            return null;
    }
}
