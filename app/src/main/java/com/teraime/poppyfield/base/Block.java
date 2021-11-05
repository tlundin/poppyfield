package com.teraime.poppyfield.base;

import java.util.List;
import java.util.Map;

public class Block {
    public static final String GIS = "block_add_gis_image_view" ;
    public static final String GIS_LAYER = "block_add_gis_layer" ;
    public static final String GIS_POINTS = "block_add_gis_point_objects";
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
