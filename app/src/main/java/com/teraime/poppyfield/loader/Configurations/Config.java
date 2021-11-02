package com.teraime.poppyfield.loader.Configurations;

import java.util.List;

public class Config<T> {

    String version;
    protected List<String> rawData;


    @SuppressWarnings("unchecked")
    public T strip(List<String> x) {
        this.version = x.remove(0).split(",")[1].trim();
        rawData=x;
        return (T)this;
    }
    public String getVersion() {
        return version;
    }

    public List<String> getRawData() { return rawData; }
}
