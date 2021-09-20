package com.teraime.poppyfield.base;

import android.util.Log;

import com.teraime.poppyfield.base.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MenuDescriptor {

    List<MenuItem> mMenu;

    public class MenuItem {
        public Map<String, String> mAttrs;
        public List<Map<String, String>> mElems;
        public void addHeader(Block b) {
                mAttrs = b.getAttrs();
            mElems = new LinkedList<>();
        }
        public void addEntry(Block b) {
            mElems.add(b.getAttrs());
        }

    }
    public List<MenuItem> getMenu() {
        return mMenu;
    }
    public MenuDescriptor(List<Block> menuBlocks) {
        mMenu = new LinkedList<>();
        MenuItem rootM = new MenuItem();
        rootM.mElems = new LinkedList<>();
        mMenu.add(rootM);
        MenuItem currGroup = rootM;

        for (Block b:menuBlocks) {

            switch (b.getBlockType()) {
                case "block_define_menu_header":
                    MenuItem mi = new MenuItem();
                    mi.addHeader(b);
                    currGroup=mi;
                    mMenu.add(mi);
                    break;
                case "block_define_menu_entry":
                    currGroup.addEntry(b);
                    break;

            }
        }
    }
}
//{text_color=#333333, bck_color=#ffb3b3, type=Start_Workflow, target=wf_logg}
//{label=SmåBiotoper, text_color=White, bck_color=#191970}