package com.teraime.poppyfield.templates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.HashMap;
import java.util.Map;

public abstract class TemplateFragment extends Fragment {
    protected WorldViewModel model;
    protected Map<String, ViewGroup> mContainers = new HashMap<>();

    @Nullable

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle _savedInstanceState, int template_id) {
        View mView = inflater.inflate(template_id, container, false);
        if (model == null)
            model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);
        return mView;
    }

    @Override
    public void onResume() {
        Log.d("LIFECYCLE","IN onResume for template "+getName());
        model.getToolBar().setTitle(model.getPageStack().getInfocusPage().getName());
        super.onResume();
    }

    public Map<String,ViewGroup> getContainers() {
        return mContainers;
    };


    public abstract String getName();
}
