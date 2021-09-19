package com.teraime.poppyfield.base;

import java.util.List;

public class Workflow {

    List<Block> blocks;

    public Workflow() {
    }

    public String getName() {
        return blocks.get(0).mAttrs.get("workflowname");
    }
    public List<Block> getBlocks() { return blocks; }
    public void addBlocks(List<Block> _blocks) {
        this.blocks = _blocks;
    }
}
