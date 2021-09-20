package com.teraime.poppyfield.templates;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LogScreen extends Fragment {
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        WorldViewModel model = new ViewModelProvider(requireActivity()).get(WorldViewModel.class);
        if (view == null)
            view = inflater.inflate(R.layout.start, container, false);
        final TextView logTV = (TextView) view.findViewById(R.id.log);
        logTV.setMovementMethod(new ScrollingMovementMethod());
        //refresh log every second?
        SpannableStringBuilder builder= new SpannableStringBuilder();
        MutableLiveData<SpannableStringBuilder> mutBuilder = new MutableLiveData<>(builder);
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                builder.clear();
                // Update the UI
                Map<String, List<String>> log = Logger.gl().debug();
                for (String k : log.keySet()) {
                    SpannableString sp = new SpannableString("\n" + k);
                    sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), (k.equals("FAILURES")) ? R.color.crimson : R.color.limegreen)
                    ), 0, sp.length(), 0);
                    sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), 0);
                    builder.append(sp);
                    List<String> l = log.get(k);
                    if (l != null) {
                        for (String s : l)
                            builder.append("\n ").append(s);
                    }
                }
                mutBuilder.postValue(builder);

            }
            }, 0, 1000);

        final Observer<SpannableStringBuilder> logObserver = b -> {
            logTV.setText(b, TextView.BufferType.SPANNABLE);
        };
        final Observer<List<Config<?>>> loadObserver = configs -> {
            if (configs.size() == 4) {
                t.cancel();
            }
        };
        mutBuilder.observe(this.getViewLifecycleOwner(),logObserver);
        model.getMyConf().observe(this.getViewLifecycleOwner(),
                loadObserver);
        return view;
    }
}
