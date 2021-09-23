package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Block {
    public static final String GIS = "block_add_gis_image_view" ;
    public static final String GIS_LAYER = "block_add_gis_layer" ;
    public static final String GIS_POINTS = "block_add_gis_point_objects";
    String id;
    Map<String,String> mAttrs;
    String blockType;

    public Block(String name, String id, Map<String, String> attrs) {
        this.id=id;
        this.mAttrs=attrs;
        this.blockType=name;
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
}
