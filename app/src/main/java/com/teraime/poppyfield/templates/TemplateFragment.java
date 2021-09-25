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

public abstract class TemplateFragment extends Fragment {
    protected WorldViewModel model;
    @Nullable

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle _savedInstanceState, int template_id) {
        View mView = inflater.inflate(template_id, container, false);
        if (model == null)
            model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);
        return mView;
    }

    @Override
    public void onResume() {
        Log.d("LIFECYCLE","IN onResume for "+getName());
        model.getToolBar().setTitle(getName());
        super.onResume();
    }


    public abstract String getName();
}
