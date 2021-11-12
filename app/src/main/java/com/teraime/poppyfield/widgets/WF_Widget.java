package com.teraime.poppyfield.widgets;

import android.util.Log;
import android.view.View;

public abstract class WF_Widget extends WF_Thing implements Drawable {

	private final View myView;
	private String label;
	private boolean isVisible;



	public WF_Widget(String id, String label, View v, boolean isVisible) {
		super(id);
		myView = v;
		if (!isVisible)
			hide();
		this.isVisible = isVisible;
		this.label = label;

	}


	@Override
	public View getWidget() {
		return myView;
	}

	@Override
	public String getLabel() {
		return label;
	}


	@Override 
	public boolean isVisible() {
		return isVisible;
	}

	@Override
	public abstract void refresh();

	@Override
	public void show() {
		Log.d("nils","Showing view ");
		myView.setVisibility(View.VISIBLE);
		isVisible = true;
	}


	@Override
	public void hide() {
		Log.d("nils","Hiding view ");
		myView.setVisibility(View.GONE);
		isVisible = false;
	}

}