package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Logger {

    private static final Map<String, List<String>> debugLog = new HashMap<>();
    private static Logger instance=null;

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


    public void e(String msg) {
        debugLog.computeIfAbsent("FAILURES", k -> new LinkedList<>());
        Objects.requireNonNull(debugLog.get("FAILURES")).add(msg);
        Log.e("FAILURES",msg);
    }


    public Map<String, List<String>> debug() {
        return debugLog;
    }



}
