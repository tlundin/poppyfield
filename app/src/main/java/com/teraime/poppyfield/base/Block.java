package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Block {
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

    public String getBlockType() {
        return blockType;
    }

    public String getBlockId() {
        return id;
    }
}
