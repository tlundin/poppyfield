package com.teraime.poppyfield.templates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.pages.Page;

public class DefaultTemplate extends TemplateFragment {
    Page mPage = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.template_wf_default_no_scroll);
        mPage = (Page)model.getPageStack().getInfocusPage();
        mPage.onCreate(this);
        return v;
    }

    @Override
    public String getName() {
        return "DefaultTemplate";
    }
}
