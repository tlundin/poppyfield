package com.teraime.poppyfield.pages;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.teraime.poppyfield.base.Context;
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

public class Page {

    protected Fragment mFragment;
    final String mTemplateType, mName;
    final protected WorldViewModel model;
    final protected Workflow workFlow;
    protected Context mWorkFlowContext;

    public Page(WorldViewModel model, String template, Workflow wf) {
        mTemplateType = template;
        if (wf != null)
            mName = wf.getName().replace("wf_", "");
        else
            mName = "Boot";

        this.workFlow = wf;
        this.model=model;


    }

    public void onCreate(Fragment f) {
        Log.d("v","On Create for page "+getName());
        mFragment=f;
        WFRunner.getVisitedBlocks(workFlow);
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
