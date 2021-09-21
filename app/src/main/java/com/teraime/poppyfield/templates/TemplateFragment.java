package com.teraime.poppyfield.templates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

public class TemplateFragment extends Fragment {
    protected View mView=null;
    protected WorldViewModel model;
    @Nullable

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, int template_id) {
        if (mView == null)
            mView = inflater.inflate(template_id, container, false);
        if (model == null)
            model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);
        return mView;
    }
}
