package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Context {

    Map<String,String> gVars,mVars,mCols;
    final static Map<String,String> NULL_MAP = new HashMap<>();
    public Map<String, String> getVariableValues() { return mVars==null?NULL_MAP:mVars; }

    public Map<String, String> getGlobalValues() {
        return gVars==null?NULL_MAP:gVars;
    }

    public Map<String, String> getColumnValues() { return mCols==null?NULL_MAP:mCols; }

    public Context(Map<String,String> globs, Map<String,String> vars, Map<String,String> cols) {
        Log.d("CONTEXT","IN CREATE CONTEXT");
        Log.d("VARS",vars.toString());
        Log.d("COLS",cols.toString());
         gVars = globs;
         mCols = cols;
         mVars = vars;

    }

    @Override
    public String toString() {
        return "\nGLOBALS:\n"+getGlobalValues().toString()+"\nVARS: \n"+getVariableValues().toString()+"\nCOLS:\n"+getColumnValues().toString();
    }
}
