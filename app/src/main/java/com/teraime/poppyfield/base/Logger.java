package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Logger {
    private static Logger instance=null;
    private static final Map<String, List<String>> debugLog = new HashMap<>();
    private static final Map<String, List<String>> errorLog = new HashMap<>();
    public static Logger gl() {
        if (instance == null)
            instance = new Logger();
        return instance;
    }

    public void d(String header, String msg) {
        debugLog.computeIfAbsent(header, k -> new LinkedList<>());
        Objects.requireNonNull(debugLog.get(header)).add(msg);
        Log.d(header,msg);
    }


    public void e(String header, String msg) {
        errorLog.computeIfAbsent(header, k -> new LinkedList<>());
        Objects.requireNonNull(errorLog.get(header)).add(msg);
        Log.e(header,msg);
    }

    public String p() {
        String r = debugLog.toString()+errorLog.toString();
        Log.d("r",r);
        return r;
    }

    public Map<String, List<String>> debug() {
        return debugLog;
    }
    public Map<String, List<String>> error() {
        return errorLog;
    }
}
