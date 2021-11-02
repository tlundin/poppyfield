package com.teraime.poppyfield.gis;

import com.teraime.poppyfield.base.Expressor;

import java.util.List;
import java.util.Map;

public class GisObject {
    private final Map<String, String> keyChain;
    protected List<Location> myCoordinates;
    private final Map<String, String> attributes;
    private String label;
    private final List<Expressor.EvalExpr> eLabel;
    public GisObject(Map<String, String> keyChain, List<Location> myCoordinates, Map<String, String> attributes) {
        this.keyChain = keyChain;
        this.myCoordinates = myCoordinates;
        this.attributes = attributes;
        eLabel = Expressor.preCompileExpression(attributes.get("label"));

    }

    public Map<String, String> getKeys() {
        return keyChain;
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }
    public String getLabel() {
        if (label!=null)
            return label;
        if (eLabel==null)
            return null;
        label = Expressor.analyze(eLabel,keyChain);
        //@notation for id
        //TODO - move to expressor
        if (label!=null && label.startsWith("@")) {
            String key = label.substring(1, label.length());
            if (key.length()>0)
                label = keyChain.get(key);

        }
        if (label==null)
            label = "";

        return label;
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

    public List<Location> getCoordinates() {
        return myCoordinates;
    }
}
