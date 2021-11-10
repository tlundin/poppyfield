package com.teraime.poppyfield.base;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.teraime.poppyfield.pages.Page;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.viewmodel.WorldViewModel;
import com.teraime.poppyfield.widgets.Drawable;
import com.teraime.poppyfield.widgets.WF_EntryField;
import com.teraime.poppyfield.widgets.WF_TextBlockWidget;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WFRunner {



    public static Map<String,List<Drawable>> getVisitedBlocks(Workflow workflow, Context workflowContext, Page p) {
        //Get all db entries for current key. Wait for it.
        boolean STOP = false;
        WorldViewModel mWorld = WorldViewModel.getStaticWorldRef();
        Log.d("w","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% "+workflow);
        Log.d("w","CONTEXT:"+workflowContext);
        Map<String,List<Drawable>>  visiBlocks = new HashMap<>();
        for (Block b: workflow.getBlocks()) {
            String dbg = null;
            if(STOP) {
                if (dbg!=null) Logger.gl().d("WFRUNNER",dbg);
                break;
            }
            String blockId = b.getBlockId();
            Log.d("v", b.getBlockType());
            Log.d("v", b.getAttrs().toString());
            String container = b.getAttr("container_name");
            List<Drawable> vbL = visiBlocks.get(container);
            if (vbL == null) {
                vbL = new LinkedList<>();
                visiBlocks.put(container,vbL);
            }
            android.content.Context androidContext = p.getFragment().getContext();
            String label = Expressor.analyze(b.getLabelExpr(),workflowContext);
            switch (b.getBlockType()) {
                case "block_set_value":
                    String varTarget = b.getAttr("target");

                    String eval = Expressor.analyze(Expressor.preCompileExpression(b.getAttr("expression")), workflowContext);
                    Block.ExecutionBehavior behavior = Block.ExecutionBehavior.valueOf(b.getAttr("execution_behavior"));
                    if (behavior == Block.ExecutionBehavior.constant && eval == null)
                        dbg = ("Stopping on "+blockId+" Reason: Eval null for "+b.getAttr("expression"));
                    STOP = true;
                    break;
                case "block_add_gis_image_view":
                case "block_add_gis_layer":
                case "block_add_gis_point_objects":
                    break;
                case "block_create_entry_field":
                    vbL.add(new WF_EntryField(
                            blockId,
                            androidContext,
                            label,
                            b.getAttr("bck_color"),
                            b.getBoolAttr("is_visible"),
                            true,
                            b.getIntAttr("text_size"),
                            b.getIntAttr("horizontal_margin"),
                            b.getIntAttr("vertical_margin")
                    ));
                            break;


                case "block_create_text_field":

                    vbL.add(new WF_TextBlockWidget(
                            blockId,
                            androidContext,
                            label,
                            b.getAttr("bck_color"),

                            b.getBoolAttr("is_visible"),
                            b.getIntAttr("text_size"),
                            b.getIntAttr("horizontal_margin"),
                            b.getIntAttr("vertical_margin")
                    ));
                    break;
                case "block_conditional_continuation":
                    eval = Expressor.analyze(Expressor.preCompileExpression(b.getAttr("expression")), workflowContext);
                    Log.d("getVisited", "EVAL: " + eval);
                case "block_jump":
                    break;
                case "block_start":
                case "block_define_page":
                case "block_no_op":
                default:
                    Log.d("v", b.getBlockType());
                    Log.d("v", b.getBlockId());

            }
        }
        Log.d("WFRUNNER","VISIBLOCKS: "+visiBlocks.toString());
        return visiBlocks;
    }

}

