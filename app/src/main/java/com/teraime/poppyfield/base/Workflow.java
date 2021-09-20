package com.teraime.poppyfield.base;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workflow {

    List<Block> blocks;
    Map<String,Block> blockM;


    public Workflow() {
    }

    public String getName() {
        return blocks.get(0).mAttrs.get("workflowname");
    }
    public List<Block> getBlocks() { return blocks; }

    public String getTemplate() throws ParseException {
        try {
        return blockM.get("block_define_page").getAttrs().get("type");

        } catch (Exception e) {
            throw new ParseException("Failed to resolve Template type from define page block",-1);
        }
    }



    public void addBlocks(List<Block> _blocks) {
        this.blocks = _blocks;
        blockM = new HashMap<>();
        for (Block b : blocks) {
          blockM.put(b.getBlockType(), b);
        }
    }



}
