package com.teraime.poppyfield.base;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workflow {

    final List<Block> blocks;
    final Map<String, List<Block>> blockM;
    final List<Expressor.EvalExpr> mContext, mLabelE;


    public Workflow(List<Block> _blocks) {
        this.blocks = _blocks;
        blockM = new HashMap<>();
        for (Block b : blocks) {
            String type = b.getBlockType();
            if (blockM.get(type) == null)
                blockM.put(type, new ArrayList<>());
            blockM.get(type).add(b);
        }
        String context = blocks.get(0).mAttrs.get("context");
        mContext = Expressor.preCompileExpression(context);
        List<Block> define_page = blockM.get("block_define_page");
        if (define_page!=null) {
            String labelE = define_page.get(0).getAttr("label");
            if (labelE != null)
                mLabelE = Expressor.preCompileExpression(labelE);
            else
                mLabelE = null;
        } else
            mLabelE = null;

        if (context != null)
            Log.d("CHANGEPAGE",context+" FOR "+this.getWorkflowName());
        else
            Log.d("CHANGEPAGE","NULL CONTEXT FOR "+this.getWorkflowName());
    }

    public String getWorkflowName() {
        return blocks.get(0).mAttrs.get("workflowname");
    }

    public List<Expressor.EvalExpr> getLabelE() {
        return mLabelE;
    }

    @Override
    public String toString() {
        return getWorkflowName();
    }

    public List<Expressor.EvalExpr> getRawContextKeys() { return mContext; }
    public List<Block> getBlocks() {
        return blocks;
    }
    public List<Block> getBlocksOfType(String type) {
        return blockM.get(type);
    }
    public Block getBlock(String blockType) {
        return blockM.get(blockType).get(0);
    }
    public boolean hasBlock(String blockType) {
        return blockM.get(blockType) != null;
    }
    public String getTemplate() throws ParseException {
        try {
            return getBlock("block_define_page").getAttrs().get("type");

        } catch (Exception e) {
            throw new ParseException("Failed to resolve Template type from define page block", -1);
        }
    }

    public void printBlocks() {
        for(Block b:getBlocks()) {
            Log.d("v", b.getBlockType() + " attr");
            Log.d("v", b.getAttrs().toString());
        }
    }



}
