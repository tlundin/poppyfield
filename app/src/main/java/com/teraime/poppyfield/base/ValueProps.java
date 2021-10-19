package com.teraime.poppyfield.base;

import java.util.HashMap;
import java.util.Map;

public class ValueProps {
    Map<String, String> values = new HashMap<>();

    public ValueProps(){};

    public ValueProps(Map<String,String> props) {
        values=props;
    }
    public ValueProps setValue(String key,String value) {
        values.put(key,value);
        return this;
    }

    public ValueProps setValue(String value) {
        values.put("value",value);
        return this;
    }

    public String getValue(String key) {
        return values.get(key);
    }
}
