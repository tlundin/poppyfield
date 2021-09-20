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
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

/**
 * 
 * @author Terje
 */

public class DefaultNoScrollTemplate extends Fragment {
	private View view;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d("vo", "GETZ");
		WorldViewModel model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);
		WFRunner.getVisiBlocks(model.getSelectedWorkFlow());
		if (view == null) {
			view = inflater.inflate(R.layout.template_wf_default_no_scroll, container, false);

		}
		return view;
	}
}
