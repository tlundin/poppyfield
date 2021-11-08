package com.teraime.poppyfield.base;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.teraime.poppyfield.pages.Page;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WFRunner {
    public static void getVisitedBlocks(Workflow workflow, Context ctx, Page p) {
        //Get all db entries for current key. Wait for it.
        WorldViewModel mWorld = WorldViewModel.getStaticWorldRef();
        Log.d("w","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% "+workflow);
        Log.d("w","CONTEXT:"+ctx);
        List<Block> visiBlocks = new LinkedList<>();
        for (Block b: workflow.getBlocks()) {
            Log.d("v",b.getBlockType());
            Log.d("v",b.getBlockId());
            Log.d("v",b.getAttrs().toString());

            switch (b.getBlockType()) {
                case "block_set_value":
                    String varTarget = b.getAttr("target");
                    String eval = Expressor.analyze(Expressor.preCompileExpression(b.getAttr("expression")),ctx);
                    Log.d("getVisited","EVAL: "+eval);
                    break;
                case "block_add_gis_image_view":
                case "block_add_gis_layer":
                case "block_add_gis_point_objects":
                    visiBlocks.add(b);
                    break;
                case "block_conditional_continuation":
                case "block_jump":
                    break;
                case "block_start":
                case "block_define_page":
                case "block_no_op":
                default:
                    Log.d("v",b.getBlockType());
                    Log.d("v",b.getBlockId());

            }

        }

    }
}
