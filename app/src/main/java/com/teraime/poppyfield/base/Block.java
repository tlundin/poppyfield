package com.teraime.poppyfield.base;

import java.util.List;
import java.util.Map;

public class Block {
    public static final String GIS = "block_add_gis_image_view" ;
    public static final String GIS_LAYER = "block_add_gis_layer" ;
    public static final String GIS_POINTS = "block_add_gis_point_objects";



    public enum ExecutionBehavior {
        constant,dynamic, constant_value, update_flow
    }
    private final List<Expressor.EvalExpr> eLabel;
    String id;
    Map<String,String> mAttrs;
    String blockType;



    public Block(String name, String id, Map<String, String> attrs) {
        this.id=id;
        this.mAttrs=attrs;
        this.blockType=name;
        eLabel = Expressor.preCompileExpression(attrs.get("label"));
    }

    public Map<String, String> getAttrs() {
        return mAttrs;
    }

    public String getAttr(String attributeName) {
        return mAttrs.get(attributeName);
    }
    public boolean getBoolAttr(String boolT) {
        String attr = getAttr(boolT);
        if (attr == null)
            return true;
        return attr.equalsIgnoreCase("true");
    }
    public int getIntAttr(String intT) {
        String attr = getAttr(intT);
        if (attr == null)
            return -1;
        return Integer.parseInt(attr);
    }
    public String getBlockType() {
        return blockType;
    }
    public String getBlockId() {
        return id;
    }
    public List<Expressor.EvalExpr> getLabelExpr() {
        return eLabel;
    }



}
