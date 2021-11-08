package com.teraime.poppyfield.base;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.teraime.poppyfield.pages.Page;
import com.teraime.poppyfield.viewmodel.WorldViewModel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class PageStack {

    final List<Page> mStack;
    final WorldViewModel model;
    final MutableLiveData<List<Page>> mPageLiveD;
    public enum EventTypes { NONE, NEW_PAGE, POP}

    private EventTypes mEvent;

    public PageStack(WorldViewModel world) {
        mStack = new ArrayList<>();
        model =world;
        mPageLiveD = new MutableLiveData<>(mStack);
        Log.d("v","Bootpage added");
        //mStack.add(Tools.createPage(model,"LogScreen",null));
        mEvent = EventTypes.NONE;
    }
    public void changePage(String target) {
        changePage(target, null);
    }

    public void changePage(String target,Context mEvalContext) {
        Log.d("PAGESTACK",target);
        if (mEvalContext == null) {
            Log.d("PAGESTACK","reusing previous context");
            //If there was a precious page use its context.
            if (getInfocusPage()!=null)
                mEvalContext = getInfocusPage().getWorkflowContext();
        }
        Workflow wf = model.getWorkFlowBundle().getWf(target);
        Logger.gl().d("CHANGEPAGE",wf.getWorkflowName()+" CONTEXT "+wf.getRawContextKeys());
        //null context == keep current
        if (wf.getRawContextKeys() != null) {
            Log.d("PAGESTACK", "generating new context: "+wf.getRawContextKeys().toString());
            LiveData<Context> ld = model.generateNewContext(Expressor.evaluate(wf.getRawContextKeys(),mEvalContext));
            ld.observeForever(context -> {
                _changePage(wf,context);
            });

        } else {
            _changePage(wf,mEvalContext);
        }
    }

    private void _changePage(Workflow wf, Context context) {
        String template=null;
        try {
            template = wf.getTemplate();
        } catch (ParseException pe) {
            Logger.gl().d("RUNTIME","Cannot open the workflow "+wf.getWorkflowName()+". Missing type argument in PageDefine block");
        }
        Log.d("v","Template "+template);

        //Check what template is required.
        if (wf.hasBlock(Block.GIS)) {
            Log.d("WARNING","Wrong template used - GIS blocks requires GisMapTemplate..substituting");
            template = "GisMapTemplate";
        }
        Page newP = Tools.createPage(model,template,wf,Expressor.analyze(wf.getLabelE(),context));
        newP.setWorkFlowContext(context);
        Page oldP = getInfocusPage();
        mStack.add(newP);
        if (oldP == null || !template.equals(oldP.getTemplateType())) {
            Log.d("Frags-new",mStack.toString());
            mEvent = EventTypes.NEW_PAGE;
            mPageLiveD.setValue(mStack);
        } else {
            Log.d("Frags-old",mStack.toString());
            newP.onCreate(oldP.getFragment());
            newP.reload();
        }

    }




    public Page getInfocusPage() {
        return mStack.isEmpty()?null:mStack.get(mStack.size()-1);
    }

    public Page getPreviousPage() { return mStack.size()<2?null:mStack.get(mStack.size()-2); }

    public LiveData<List<Page>> getPageLive() {
        return mPageLiveD;
    }

    //If false, no pop. Ask user if he wants to exit the app.
    public void pop() {
        if (mStack.size()==1) {
            Log.d("v","stack empty");
            return;
        }

        Page infocusPage = getInfocusPage();
        Page previousPage = mStack.get(mStack.size()-2);
        mStack.remove(mStack.size()-1);
        if (!previousPage.getTemplateType().equals(infocusPage.getTemplateType())) {
            mEvent = EventTypes.POP;
            Log.d("Frags","Pop - different Fragment type. Infocus: "+getInfocusPage().getName()+" previous: "+previousPage.getName());
            mPageLiveD.setValue(mStack);
        } else {
            Log.d("Frags","Pop - same Fragment type. Infocus: "+getInfocusPage().getName()+" previous: "+previousPage.getName());
            getInfocusPage().reload();
        }

    }


    public EventTypes consumeEvent() {
        EventTypes ret = mEvent;
        mEvent = EventTypes.NONE;
        return ret;
    }
}
