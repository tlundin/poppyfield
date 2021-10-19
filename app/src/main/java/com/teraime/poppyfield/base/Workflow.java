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
    final List<Expressor.EvalExpr> mContext;


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
        if (context != null)
            Log.d("CHANGEPAGE",context+" FOR "+this.getName());
        else
            Log.d("CHANGEPAGE","NULL CONTEXT FOR "+this.getName());
    }

    public String getName() {
        Log.d("ATTRS",blocks.get(0).getAttrs().toString());
        return blocks.get(0).mAttrs.get("workflowname");
    }

    public List<Expressor.EvalExpr> getContext() { return mContext; }
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
