package com.teraime.poppyfield;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.base.WFRunner;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.templates.TemplateFragment;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    MenuDescriptor menuDescriptor = null;
    MaterialToolbar topAppBar;
    DrawerLayout drawerLayout;
    WorldViewModel model;
    Boolean appEntry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        model = new ViewModelProvider(this).get(WorldViewModel.class);
        final NavigationView navi = findViewById(R.id.nav);
        navi.setItemIconTintList(null);
        topAppBar = this.findViewById(R.id.topAppBar);
        model.setToolBar(topAppBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        setSupportActionBar(topAppBar);
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        if (model.getInfocusPage() == null) {
            appEntry = true;
            try {
                Fragment logTVF = Tools.createFragment("LogScreen");
                setContentView(logTVF, "Boot");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,model.getPage(model.getInfocusPage()),model.getInfocusPage()).commit();

        final Observer<List<Config<?>>> loadObserver = configs -> {
            if (configs.size() == 4 ) {
                populateMenu(navi.getMenu(), model);
                Logger.gl().d("MORTIS", "DONE");
                if (appEntry) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        };
        model.getMyConf().observe(this,
                loadObserver);

    }


    private void populateMenu(Menu menu, WorldViewModel model) {
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
                        String template="NONE";
                        try {
                            template = wf.getTemplate();
                        } catch (ParseException pe) {
                            Logger.gl().d("RUNTIME","Cannot open the workflow "+wf.getName()+". Missing type argument in PageDefine block");
                        }
                        Log.d("v","Template "+template);
                        model.setSelectedWorkFlow(wf);
                        //Check what template is required.
                        //TODO: Remove - make sure correct template used.
                        if (wf.hasBlock(Block.GIS)) {
                            Log.d("WARNING","Wrong template used - GIS blocks requires GisMapTemplate..substituting");
                            template = "GisMapTemplate";
                        }
                        try {
                            Fragment templateF = Tools.createFragment(template);
                            setContentView(templateF,wf.getName());

                        } catch (ClassNotFoundException e) {
                            Logger.gl().e(e.getMessage());
                        }
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    });
                }
            }

        }
    }

    private void setContentView(Fragment templateF, String name) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft
                .replace(R.id.content_frame, templateF,name)
                .addToBackStack(name)
                .commit();
        model.setPage(templateF,name);
        Log.d("REFFO","In SETCONTENT: "+getSupportFragmentManager().getFragments().toString());
    }


}