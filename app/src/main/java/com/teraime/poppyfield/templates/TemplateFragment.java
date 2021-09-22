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

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

public abstract class TemplateFragment extends Fragment {
    private View mView=null;
    protected WorldViewModel model;
    @Nullable

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, int template_id) {
        mView = inflater.inflate(template_id, container, false);
        if (model == null)
            model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);
        return mView;
    }

    @Override
    public void onResume() {
        model.setPage(this,getName());
        Log.d("RESUME","IN RESUME FOR "+getName());
        model.getToolBar().setTitle(getName());
        Log.d("REFFO","In onRESUME: "+this.getActivity().getSupportFragmentManager().getFragments().toString());
        super.onResume();
    }

    public abstract String getName();
}
