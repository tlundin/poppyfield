package com.teraime.poppyfield.templates;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.concurrent.atomic.AtomicInteger;

public class LoadFragment extends Fragment {

    private WorldViewModel model;
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle _savedInstanceState) {

            View v = inflater.inflate(R.layout.load_fragment, container, false);
            if (model == null)
                model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);

            TextView tv = v.findViewById(R.id.loadHeader);
            ProgressBar pb = v.findViewById(R.id.progressBar);
            LiveData<String> progress = model.getLogObservable();
            Drawable progressDrawable = pb.getProgressDrawable().mutate();
            progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            pb.setProgressDrawable(progressDrawable);

            if (model.isAppEntry() && !model.loadDone()) {
                AtomicInteger x = new AtomicInteger();
                final Observer<String> logObserver = subject -> {
                    pb.setMax(model.getModuleCount());
                    tv.setText(subject);
                    pb.setProgress(x.getAndIncrement(), true);
                };

                progress.observe(this.getViewLifecycleOwner(), logObserver);
                model.startLoad();
            }
            model.getToolBar().setTitle("");
            return v;

        }

}
