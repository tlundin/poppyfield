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

public class LogScreen extends TemplateFragment {

    final SpannableStringBuilder builder= new SpannableStringBuilder();

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState,R.layout.start);
        final TextView logTV = (TextView) view.findViewById(R.id.log);
        logTV.setMovementMethod(new ScrollingMovementMethod());

        MutableLiveData<String> mutBuilder = model.getLogObservable();

        final Observer<String> logObserver = subject -> {
            buildLog(subject);
            logTV.setText(builder, TextView.BufferType.SPANNABLE);
        };
        mutBuilder.observe(this.getViewLifecycleOwner(),logObserver);
        return view;
    }

    private void buildLog(String subject) {
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

    }

    @Override
    public String getName() {
        return "Boot";
    }
}
