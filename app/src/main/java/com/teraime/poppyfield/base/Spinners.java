package com.teraime.poppyfield.base;
import androidx.annotation.NonNull;

import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.parsers.SpinnerParser;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Spinners extends Config<Spinners> {
    public static class SpinnerElement implements Serializable {
        private static final long serialVersionUID = 9162426573700197032L;
        public final String value;
        public final String opt;
        public final String descr;
        public final List<String> varMapping = new ArrayList<>();
        public SpinnerElement(String val,String opt,String vars,String descr) {
            this.value = val;
            this.opt = opt.replace("\"", "");
            this.descr=descr.replace("\"", "");
            if (vars!=null&&!vars.isEmpty()) {
                String[] v = vars.split("\\|");
                Collections.addAll(varMapping, v);
            }
         }
    }

    private Map<String,List<SpinnerElement>> myElements;

    public List<SpinnerElement> get(String spinnerId){
        return myElements.get(spinnerId.toLowerCase());
    }
    public void add(String id,List<SpinnerElement> l) {
        myElements.put(id.toLowerCase(), l);
    }
    public int size() {
        return myElements.size();
    }

    public Spinners parse() throws ParseException {
        this.myElements = SpinnerParser.parse(rawData);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return myElements.toString();
    }
}
