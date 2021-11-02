package com.teraime.poppyfield.loader;

import androidx.lifecycle.LiveData;

import com.teraime.poppyfield.room.VariableTable;

import java.io.IOException;
import java.util.List;

public interface LoaderCb {
    public void loaded(List<String> file) throws IOException;
}
