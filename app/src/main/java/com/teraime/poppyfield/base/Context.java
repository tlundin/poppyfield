package com.teraime.poppyfield.base;

import android.util.Log;

import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    public class VariableCache {
        Map<String,Variable> cache = new HashMap<>();
        public Variable getVariable(String varName) {
            Variable v = cache.get(varName);
            if (v ==null) {
                v = new Variable(varName, mTable, Context.this);
                cache.put(varName, v);
            }
            return v;
        }

        public void persist() {
            List<Variable> varsToSave = new ArrayList<>();
            for (Variable v:cache.values()) {
                if (v.hasChanged()) {
                    varsToSave.add(v);
                }
            }
            WorldViewModel.getStaticWorldRef().persistUserInput(varsToSave);
        }
    }
    Map<String,String> gVars,mVars,mCols;
    final static Map<String,String> NULL_MAP = new HashMap<>();
    private final VariableCache mCache;
    private final Table mTable;

    public Context(Map<String,String> globs, Map<String,String> vars, Map<String,String> cols) {
        Log.d("CONTEXT","IN CREATE CONTEXT");
        Log.d("GLOBS",(globs==null)?"NULL":globs.toString());
        Log.d("VARS",vars.toString());
        Log.d("COLS",cols.toString());
         gVars = globs;
         mCols = cols;
         mVars = vars;
         mCache = new VariableCache();
         mTable = WorldViewModel.getStaticWorldRef().getTable();
    }

    @Override
    public String toString() {
        return "\nGLOBALS:\n"+getGlobalValues().toString()+"\nVARS: \n"+getVariableValues().toString()+"\nCOLS:\n"+getColumnValues().toString();
    }

    public VariableCache getVariableCache() {
        return mCache;
    }


    public Map<String, String> getVariableValues() { return mVars==null?NULL_MAP:mVars; }

    public Map<String, String> getGlobalValues() {
        return gVars==null?NULL_MAP:gVars;
    }

    public Map<String, String> getColumnValues() { return mCols==null?NULL_MAP:mCols; }

}
