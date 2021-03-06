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
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Terje
 */

public class DefaultNoScrollTemplate extends TemplateFragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return super.onCreateView(inflater,container,savedInstanceState,R.layout.template_wf_default_no_scroll);
	}



	@Override
	public String getName() {
		return "DefaultNoScrollTemplate";
	}
}
