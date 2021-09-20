package com.teraime.poppyfield.base;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class WFRunner {
    public static List<Block> getVisitedBlocks(Workflow workflow) {
        Log.d("w","Executing simul run for "+workflow);
        List<Block> visiBlocks = new LinkedList<>();
        for (Block b: workflow.getBlocks()) {
            switch (b.getBlockType()) {
                case "block_add_gis_image_view":
                case "block_add_gis_layer":
                case "block_add_gis_point_objects":
                    visiBlocks.add(b);
                    break;
                case "block_start":
                case "block_define_page":
                case "block_no_op":
                default:
                    Log.d("v",b.getBlockType());
                    Log.d("v",b.getBlockId());

            }

        }
        return visiBlocks;
    }
}
