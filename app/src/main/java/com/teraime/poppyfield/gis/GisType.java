package com.teraime.poppyfield.gis;

import android.util.JsonReader;
import android.util.Log;

import com.teraime.poppyfield.base.Config;
import com.teraime.poppyfield.loader.GeoJsonParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class GisType extends Config {
    String rawGeoData;
    String version,type;
    private List<GisObject> geoObjs;

    public GisType()  {
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

    public String getVersion() {
        return version;
    }
}
