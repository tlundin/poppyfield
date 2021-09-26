package com.teraime.poppyfield;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.teraime.poppyfield.base.Logger;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.PageStack;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.loader.Configurations.Config;
import com.teraime.poppyfield.templates.Page;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    MenuDescriptor menuDescriptor = null;
    MaterialToolbar topAppBar;
    DrawerLayout drawerLayout;
    WorldViewModel model;
    Boolean appEntry = false;
    DialogInterface.OnClickListener dialogClickListener;

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
        PageStack stack = model.getPageStack();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame,
                        Tools.createFragment(stack.getInfocusPage().getTemplateType()),
                        stack.getInfocusPage().getName()).commit();
        Log.d("Frags",getSupportFragmentManager().getFragments().toString());
        final Observer<List<Config<?>>> loadObserver = configs -> {
            if (configs.size() == 4 ) {
                populateMenu(navi.getMenu(), model);
                Logger.gl().d("MORTIS", "DONE");
                drawerLayout.openDrawer(GravityCompat.START);
            }
        };
        model.getMyConf().observe(this,
                loadObserver);

        final Observer<List<Page>> pageObserver = pages -> {
            if (stack.hasNewPage()) {
                Log.d("velcro","Adding");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,
                                Tools.createFragment(stack.getInfocusPage().getTemplateType()),
                                stack.getInfocusPage().getName())
                        .addToBackStack(stack.getInfocusPage().getName())
                        .commit();
                Log.d("Frags",getSupportFragmentManager().getFragments().toString());
            } else {
                Log.d("v","Popping");
                Log.d("Frags-popb",getSupportFragmentManager().getFragments().toString());
                getSupportFragmentManager().popBackStack();
                List<Fragment> fs = getSupportFragmentManager().getFragments();
                Log.d("Frags-popa",fs.toString());
                //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,fs.get(fs.size()-1),stack.getInfocusPage().getName());

            }
        };
        stack.getPageLive().observe(this,pageObserver);
    }


    private void populateMenu(Menu menu, WorldViewModel model) {
        Workflow main = model.getWorkFlowBundle().getMainWf();
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
                        model.getPageStack().changePage(elem.get("target"));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    });
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        Log.d("v","BACK PRESSED");
        model.getPageStack().pop();
    }

}