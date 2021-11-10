package com.teraime.poppyfield.widgets;

import android.view.ViewGroup;
import com.teraime.poppyfield.base.Logger;


public abstract class WF_Thing {

	private final String myId;
	private ViewGroup myWidget;
	protected Logger o;

	protected WF_Thing(String id) {
		o = Logger.gl();
		myId = id;
	}
	
	public String getId() {
		return myId;
	}



}