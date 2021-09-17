package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Block {
    String id;
    Map<String,String> mAttrs;

    public Block(String name, String id, Map<String, String> attrs) {
        this.id=id;
        this.mAttrs=attrs;
    }

    public String getBlockId() {
        return id;
    }
}
