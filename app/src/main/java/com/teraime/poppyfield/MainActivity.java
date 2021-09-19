package com.teraime.poppyfield;

import static android.view.Menu.NONE;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    MenuDescriptor menuDescriptor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WorldViewModel model = new ViewModelProvider(this).get(WorldViewModel.class);
        final TextView logTV = this.findViewById(R.id.log);
        final MaterialToolbar topAppBar = this.findViewById(R.id.topAppBar);
        final DrawerLayout drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.id.drawerLayout);
        logTV.setMovementMethod(new ScrollingMovementMethod());
        // Create the observer which updates the UI.
        final Observer<List<Config>> loadObserver = configs -> {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            // Update the UI
            Map<String, List<String>> log = Logger.gl().debug();
            for (String k : log.keySet()) {
                SpannableString sp = new SpannableString("\n" + k);
                sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), (k.equals("FAILURES")) ? R.color.crimson : R.color.limegreen)
                ), 0, sp.length(), 0);
                sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), 0);
                builder.append(sp);
                List<String> l = log.get(k);
                if (l != null) {
                    for (String s : l)
                        builder.append("\n ").append(s);
                }
            }
            logTV.setText(builder, TextView.BufferType.SPANNABLE);
            if (configs.size() == 4) {
                Logger.gl().d("LOADER", "DONE");
                Workflow main = Loader.getInstance().getBundle().getMainWf();
                menuDescriptor = new MenuDescriptor(main.getBlocks());

            }

        };

        model.getMyConf().observe(this,
                loadObserver);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });


        navigationView.setNavigationItemSelectedListener { menuItem ->
                // Handle menu item selected
                menuItem.isChecked = true;
            drawerLayout.close();
        }
    }




/*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d("v","gets");
        if (menuDescriptor !=null ) {
            for (MenuDescriptor.MenuItem md:menuDescriptor.getMenu()){
                String label = md.mAttrs == null?"base":md.mAttrs.get("label");
                NavigationView navMenu = findViewById(R.id.nav);
                navMenu.getMenu().add(R.id.wf,NONE,NONE,label);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_drawer, menu);
        return true;
    }
*/

}