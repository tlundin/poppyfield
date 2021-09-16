package com.teraime.poppyfield;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WorldViewModel model = new ViewModelProvider(this).get(WorldViewModel.class);
        final TextView log = (TextView) this.findViewById(R.id.log);
        // Create the observer which updates the UI.
        final Observer <List<String>> loadObserver = new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> m) {
                // Update the UI, in this case, a TextView.
                log.setText(m.toString());

            }
        };

        model.getLoadProgress().observe(this,loadObserver);
    }
}