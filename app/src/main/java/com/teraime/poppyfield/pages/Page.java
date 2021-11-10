package com.teraime.poppyfield.pages;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Context;
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.templates.TemplateFragment;
import com.teraime.poppyfield.viewmodel.WorldViewModel;
import com.teraime.poppyfield.widgets.Drawable;

import java.util.List;
import java.util.Map;

public class Page {

    protected TemplateFragment mFragment;
    final String mTemplateType;
    final protected WorldViewModel model;
    final protected Workflow workFlow;
    protected Context mWorkFlowContext;
    final String mName;

    public Page(WorldViewModel model, String template, Workflow wf, String name) {
        mTemplateType = template;
        this.workFlow = wf;
        this.model=model;
        this.mName = name;
    }

    public void onCreate(TemplateFragment f) {
        Log.d("v","On Create for page "+getName());
        mFragment=f;
    }

    public String getTemplateType() {
        return mTemplateType;
    }

    public String getName() {
        return mName;
    }

    public TemplateFragment getFragment() {
        return mFragment;
    }


    public void reload() {

        Map<String,List<Drawable>> blocksToDraw = WFRunner.getVisitedBlocks(workFlow, mWorkFlowContext, this);
        Log.d("Drawables",blocksToDraw.toString());
        draw(blocksToDraw);

    };

    public void draw(Map<String,List<Drawable>> blocksToDraw) {
        Map<String, ViewGroup> containers = mFragment.getContainers();
        for (String container:blocksToDraw.keySet()) {
            for(Drawable blockToDraw:blocksToDraw.get(container)) {
                Log.d("PAGE","Added "+blockToDraw.toString()+" to "+container);
                containers.get(container).addView(blockToDraw.getWidget());
            }
        }
    }

    public void setWorkFlowContext(Context context) {
        Log.d("obx_context","Setting context to: "+((context == null)?"NULL":context.toString()));
        mWorkFlowContext = context;
    }
    public Context getWorkflowContext() {
        return mWorkFlowContext;
    }

}
