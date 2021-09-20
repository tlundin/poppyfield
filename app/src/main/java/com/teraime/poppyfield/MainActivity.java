package com.teraime.poppyfield;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
        setContentView(R.layout.activity_layout);
        WorldViewModel model = new ViewModelProvider(this).get(WorldViewModel.class);
        final TextView logTV = this.findViewById(R.id.log);
        final MaterialToolbar topAppBar = this.findViewById(R.id.topAppBar);
        logTV.setMovementMethod(new ScrollingMovementMethod());
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        final NavigationView navi = findViewById(R.id.nav);
        navi.setItemIconTintList(null);
        setSupportActionBar(topAppBar);
        // Create the observer which updates the UI.
        final Observer<List<Config<?>>> loadObserver = configs -> {
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
                populateMenu(navi.getMenu());
            }

        };

        model.getMyConf().observe(this,
                loadObserver);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });


    }


    private void populateMenu(Menu menu) {
    Workflow main = Loader.getInstance().getBundle().getMainWf();
    menuDescriptor = new MenuDescriptor(main.getBlocks());

    List<MenuDescriptor.MenuItem> menuTopology = menuDescriptor.getMenu();
    int orderIdx = 101;
    int itemId = 2;
    for (MenuDescriptor.MenuItem item:menuTopology) {
        //first one is root, no header.
        if (item.mAttrs != null) {
            MenuItem header = menu.add(R.id.wf_group,itemId++,orderIdx++,item.mAttrs.get("label"));
            header.setCheckable(false);
            header.setEnabled(false);
        }
        if (item.mElems != null){
            for (Map<String, String> elem : item.mElems) {
                Log.d("KOOK",item.mElems.toString());
                MenuItem entry = menu.add(R.id.wf_group, itemId++, orderIdx++, elem.get("target"));
                entry.setOnMenuItemClickListener(item1 -> {
                    Log.d("VOOF",elem.get("target"));
                    Workflow wf = Loader.getInstance().getBundle().getWf(elem.get("target"));
                    Log.d("v","WF has"+wf.getBlocks().size()+" blocks");
                    return true;
                });
            }
        }

    }
}



}