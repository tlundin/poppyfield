package com.teraime.poppyfield.loader.Configurations;

import android.util.Xml;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.loader.parsers.WorkflowConfigurationParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowBundle extends Config<WorkflowBundle> {

    String sData;
    WorkflowConfigurationParser.WorkFlowBundleDescriptor wd;
    Map<String,Workflow> wfMap;

    public WorkflowBundle stringify(List<String> data) {
        StringBuilder sb=new StringBuilder();
        for(String s:data) {
            sb.append(s);
            sb.append("\n");
        }
        sData=sb.toString();
        rawData = data;
        return this;
    }

    public List<Workflow> getWfs() {
        return wd.bundle;
    }

    public Workflow getMainWf() {
        return wd.bundle.get(0);
    }

    public WorkflowBundle parse() throws XmlPullParserException, IOException, ParseException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(sData));
        wd = WorkflowConfigurationParser.parse(parser);
        createMap();
        version = wd.appVersion;
        return this;
    }

    public Workflow getWf(String target) {
        return wfMap.get(target);
    }

    private void createMap() throws ParseException {
        wfMap = new HashMap<>();
        try {
        for (Workflow wf:wd.bundle)
            wfMap.put(wf.getWorkflowName(),wf);
        } catch (Exception e) {
            Logger.gl().e("Workflow missing name");
            throw new ParseException("Failed to extract name from Workflow",-1);

        }
    }
}
