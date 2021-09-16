package com.teraime.poppyfield.base;

import com.teraime.poppyfield.gis.GisType;

import java.io.IOException;
import java.util.List;

public abstract class Config {

    String version;
    String rawData;

    public Config strip(List<String> x) {
        this.version = x.remove(0).split(",")[1].trim();
        StringBuilder sb=new StringBuilder();
        for(String s:x) {
            sb.append(s);
            sb.append("\n");
        }
        rawData=sb.toString();
        return this;
    }

    public abstract Config parse(String type) throws IOException;
}
