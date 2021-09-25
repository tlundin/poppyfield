package com.teraime.poppyfield.base;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.teraime.poppyfield.templates.Page;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class PageStack {

    final List<Page> mStack;
    final WorldViewModel model;
    final MutableLiveData<List<Page>> mPageLiveD;
    boolean hasNewPage = false;

    public PageStack(WorldViewModel world) {
        mStack = new ArrayList<>();
        model =world;
        mPageLiveD = new MutableLiveData<>(mStack);
        Log.d("v","Bootpage added");
        mStack.add(Tools.createPage(model,"LogScreen",null));
        mPageLiveD.setValue(mStack);
    }

    public void changePage(String target) {
        Workflow wf = model.getWorkFlowBundle().getWf(target);
        Log.d("v","WF has"+wf.getBlocks().size()+" blocks");
        String template=null;
        try {
            template = wf.getTemplate();
        } catch (ParseException pe) {
            Logger.gl().d("RUNTIME","Cannot open the workflow "+wf.getName()+". Missing type argument in PageDefine block");
        }
        Log.d("v","Template "+template);

        //Check what template is required.
        //TODO: Remove - make sure correct template used.
        if (wf.hasBlock(Block.GIS)) {
            Log.d("WARNING","Wrong template used - GIS blocks requires GisMapTemplate..substituting");
            template = "GisMapTemplate";
        }
        Page newP = Tools.createPage(model,template,wf);
        Page oldP = getInfocusPage();
        mStack.add(newP);
        if (!template.equals(oldP.getTemplateType())) {
            hasNewPage=true;
            mPageLiveD.setValue(mStack);
        } else {
            newP.onCreate(oldP.getFragment());
            newP.reload();
        }
    }


    public Page getInfocusPage() {
        return mStack.isEmpty()?null:mStack.get(mStack.size()-1);
    }

    public LiveData<List<Page>> getPageLive() {
        return mPageLiveD;
    }

    public boolean hasNewPage() {
        return hasNewPage;
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
            hasNewPage = false;
            mPageLiveD.setValue(mStack);
        } else {
            Log.d("v","mstack: "+mStack.toString());
            Log.d("focus","focus "+getInfocusPage().getName());
            getInfocusPage().reload();
        }

    }


}
