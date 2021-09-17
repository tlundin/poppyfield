package com.teraime.poppyfield.base;

import java.util.List;

public class Config<T> {

    String version;
    protected List<String> rawData;

    public T strip(List<String> x) {
        this.version = x.remove(0).split(",")[1].trim();
        rawData=x;
        return (T)this;
    }

}
