package com.teraime.poppyfield.loader.Configurations;

import android.util.Xml;

import com.teraime.poppyfield.loader.parsers.WorkflowConfigurationParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.List;

public class WorkflowBundle extends Config<WorkflowBundle> {

    String sData;
    WorkflowConfigurationParser.WorkFlowBundleDescriptor wd;

    public WorkflowBundle stringify(List<String> data) {
        StringBuilder sb=new StringBuilder();
        for(String s:data) {
            sb.append(s);
            sb.append("\n");
        }
        sData=sb.toString();
        return this;
    }

    public WorkflowBundle parse() throws XmlPullParserException, IOException, ParseException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(sData));
        wd = WorkflowConfigurationParser.parse(parser);
        version = wd.appVersion;
        return this;
    }
}
