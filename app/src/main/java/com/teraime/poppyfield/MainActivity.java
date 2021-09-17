package com.teraime.poppyfield;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WorldViewModel model = new ViewModelProvider(this).get(WorldViewModel.class);
        final TextView log = this.findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());
        // Create the observer which updates the UI.
        final Observer <Logger> loadObserver = m -> {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            // Update the UI
            Map<String, List<String>> d;
            d = m.error();
            for (String k:m.error().keySet()) {
                SpannableString redSpannable= new SpannableString("\n"+k);
                redSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.crimson)
                ), 0, redSpannable.length(), 0);
                redSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, redSpannable.length(), 0);
                builder.append(redSpannable);
                List<String> l = d.get(k);
                if (l != null) {
                    for (String s:l)
                        builder.append("\n ").append(s);
                }
            }
            d = m.debug();
            for (String k:m.debug().keySet()) {
                SpannableString greenSpannable= new SpannableString("\n"+k);
                greenSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.limegreen)
                ), 0, greenSpannable.length(), 0);
                greenSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, greenSpannable.length(), 0);
                builder.append(greenSpannable);
                List<String> l = d.get(k);
                if (l != null) {
                    for (String s:l)
                        builder.append("\n ").append(s);
                }
            }
            log.setText(builder, TextView.BufferType.SPANNABLE);
        };
        model.getLoadProgress().observe(this,loadObserver);
    }
}