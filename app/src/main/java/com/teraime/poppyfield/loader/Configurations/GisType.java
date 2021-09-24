package com.teraime.poppyfield.loader.Configurations;

import android.util.JsonReader;

import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.loader.parsers.GeoJsonParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class GisType extends Config<GisType> {
    String type,rawGeoData;
    private List<GisObject> geoObjs;

    public GisType stringify() {
        StringBuilder sb=new StringBuilder();
        for(String s:rawData) {
            sb.append(s);
            sb.append("\n");
        }
        rawGeoData=sb.toString();
        return this;
    }

    public GisType parse(String type) throws IOException {
        this.type=type;
        geoObjs = GeoJsonParser.parse(new JsonReader(new StringReader(rawGeoData)),type);
        return this;
    }

    public List<GisObject> getGeoObjects() {
        return geoObjs;
    }

    public String getType() {
        return type;
    }


    public List<String> getRawData() {
        return rawData;
    }
}
