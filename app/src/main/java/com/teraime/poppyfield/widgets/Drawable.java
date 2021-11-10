package com.teraime.poppyfield.widgets;

import android.view.View;

public interface Drawable {		
	View getWidget();
	String getLabel();
	void show();
	void hide();
	boolean isVisible();
}
