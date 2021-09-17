package com.teraime.poppyfield.loader.parsers;

import android.util.Log;

import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.Workflow;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkflowConfigurationParser {

    private static String language="se";

    public static class WorkFlowBundleDescriptor {
        public String appVersion;
        //workflows will be added to this one.
        public List<Workflow> bundle = new ArrayList<Workflow>();
    }

    public static WorkFlowBundleDescriptor parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        String imageMetaFormat;
        String myApplication;
        WorkFlowBundleDescriptor wfD = new WorkFlowBundleDescriptor();
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "bundle");
        myApplication = parser.getAttributeValue(null, "application");
        float appVersion=-1,newWorkflowVersion = -1;
        try {
            newWorkflowVersion = Float.parseFloat(parser.getAttributeValue(null, "version"));
            wfD.appVersion = parser.getAttributeValue(null, "app_version");
        }
        catch (Exception e) {
            Logger.gl().e("PARSE", "WorkflowBundle:No app version, or no workflowversion.");

            throw new ParseException("No appversion and/or workflow-version. Will default to 0. Please add.",0);
        }
        String minVersion = parser.getAttributeValue(null, "minVortexVersion");
        //this determines if the image meta data is in file or xml format.
        imageMetaFormat = parser.getAttributeValue(null,"img_meta_format");
        Log.d("franzon","imagemetaformat "+(imageMetaFormat==null?"null":imageMetaFormat));
        Log.d("franzon","minvortexversion "+(minVersion==null?"null":minVersion));
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (parser.getName().equals("language")) {
                Logger.gl().d("PARSE","Language set to: "+language);
                language = readText("language",parser);
            }
            else if (name.equals("workflow")) {
                //Add workflow to bundle, return a count.
                wfD.bundle.add(readWorkflow(myApplication, parser));

            } else {
                skip(name,parser);
            }
        }
        return wfD;
    }
    private static Workflow readWorkflow(String myApplication, XmlPullParser parser) throws XmlPullParserException, IOException {

        Workflow wf = new Workflow(myApplication);
        parser.require(XmlPullParser.START_TAG, null, "workflow");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("blocks")) {
                wf.addBlocks(readBlocks(parser));
            } else {
                skip(name,parser);
            }
        }

        return wf;


    }


    private static List<Block> readBlocks(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Block> blocks=new ArrayList<Block>();
        parser.require(XmlPullParser.START_TAG, null,"blocks");
        String name="";
        try {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                blocks.add(createBlock(parser.getName(),parser));
                }
        } catch (XmlPullParserException e) {
            Logger.gl().e("vortex","Got parse error when reading "+name+" on line "+e.getLineNumber());
            Logger.gl().e("vortex","Cause: "+e.getCause());
            Logger.gl().e("vortex","Message: "+e.getMessage());
            throw e;
        }
        //Check that no block has the same ID
        Set<String> tempSet = new HashSet<String>();
        for (Block b:blocks)  {
            if (!tempSet.add(b.getBlockId())) {
                Logger.gl().e("PARSE","Duplicate Block ID "+b.getBlockId());
                return blocks;
            }
        }
        return blocks;
    }


    private static Block createBlock(String blockName, XmlPullParser parser) throws IOException, XmlPullParserException {
        //o.addRow("Parsing block: block_start...");
        Map<String,String> attrs = new HashMap<>();
        String workflowName=null; String args[]=null,context=null,id=null;
        parser.require(XmlPullParser.START_TAG, null,blockName);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name= parser.getName();
            if (name.equals("block_ID")) {
                id = readText("block_ID",parser);
            } else  {
                attrs.put(name,(readText(name,parser)));
            }
        }
        return new Block(blockName,id,attrs);
    }


    // Read string from tag.
    protected static String readText(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null,tag);
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, null,tag);
        if (text==null || text.isEmpty())
            return null;
        else
            return text;
    }
    // Extract string values.
    protected static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }



    //Skips entry...return one level up in recursion if end reached.
    protected static void skip(String name, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            Logger.gl().e("PARSE","IllegalStateException while trying to read START_TAG");
            throw new IllegalStateException();
        }
        if ("workflow".equals(name)) {
            Logger.gl().e("PARSE","Closing tag for workflow missing. Aborting");
            throw new XmlPullParserException("Workflow closing tag missing");
            } else 
                Logger.gl().d("PARSE",("Skipped TAG: ["+name+"]"));
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
