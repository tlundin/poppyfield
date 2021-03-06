package com.teraime.poppyfield;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.MenuDescriptor;
import com.teraime.poppyfield.base.PageStack;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.templates.LoadFragment;
import com.teraime.poppyfield.pages.Page;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    MaterialToolbar topAppBar;
    DrawerLayout drawerLayout;
    WorldViewModel model;
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

        if (model.isAppEntry()) {
            //Present load screen
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame,
                            new LoadFragment())
                    .commit();
        }
        final Observer<String> loadObserver = ping -> {
            if (ping.equals("GisObjects") && model.getModuleCount()>0)
                model.prepareGeoData();
            else if (ping.equals("done") || ping.equals("Table") && model.getModuleCount() == 0) {
                populateMenu(navi.getMenu(), model.getMenuDescriptor());
                if (model.isAppEntry()) {
                    drawerLayout.openDrawer(GravityCompat.START);
                    model.appEntryDone();
                }
                model.setAllGisTypesLoaded();
            }
        };


        final Observer<List<Page>> pageObserver = pages -> {

            PageStack.EventTypes event = stack.consumeEvent();
            if (event == PageStack.EventTypes.NEW_PAGE) {
                Log.d("Frags","Adding");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,
                                Tools.createFragment(stack.getInfocusPage().getTemplateType()),
                                stack.getInfocusPage().getName())
                        .addToBackStack(stack.getInfocusPage().getName())
                        .commit();
                Log.d("Frags-np",getSupportFragmentManager().getFragments().toString());
            } else if (event == PageStack.EventTypes.POP) {
                Log.d("Frags","Popping");
                getSupportFragmentManager().popBackStack();
                List<Fragment> fs = getSupportFragmentManager().getFragments();
                Log.d("Frags-popa",fs.toString());
            } else {
                Log.d("Frags","EventType: "+event.name());
            }
        };



        stack.getPageLive().observe(this,pageObserver);
        model.getLogObservable().observe(this, loadObserver);
    }


    private void populateMenu(Menu menu, MenuDescriptor menuDescriptor) {
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
                    MenuItem entry = menu.add(R.id.wf_group, itemId++, orderIdx++, Expressor.analyze(model.getWorkFlowBundle().getWf(elem.get("target")).getLabelE(),null));
                    entry.setOnMenuItemClickListener(item1 -> {
                        Log.d("MainActivity","menu item clicked");
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