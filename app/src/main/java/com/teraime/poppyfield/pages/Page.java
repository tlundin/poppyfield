package com.teraime.poppyfield.pages;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.teraime.poppyfield.base.Context;
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

public class Page {

    protected Fragment mFragment;
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

    public void onCreate(Fragment f) {
        Log.d("v","On Create for page "+getName());
        mFragment=f;
        WFRunner.getVisitedBlocks(workFlow,mWorkFlowContext, this);
    }

    public String getTemplateType() {
        return mTemplateType;
    }

    public String getName() {
        return mName;
    }

    public Fragment getFragment() {
        return mFragment;
    }


    public void reload() {};

    public void setWorkFlowContext(Context context) {
        Log.d("obx_context","Setting context to: "+((context == null)?"NULL":context.toString()));
        mWorkFlowContext = context;
    }
    public Context getWorkflowContext() {
        return mWorkFlowContext;
    }

}
