package com.teraime.poppyfield.widgets;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Tools;


public class WF_TextBlockWidget extends WF_Text_Widget {

	public WF_TextBlockWidget(String id,Context ctx, String label, String backgroundColor,String text_color,  boolean isVisible, int textSize, int horizontalMargin, int verticalMargin) {
		super(id, ctx,label,backgroundColor,"black", LayoutInflater.from(ctx).inflate(R.layout.text_block,null), isVisible,textSize,horizontalMargin,verticalMargin);
	}


	@Override
	public void refresh() {
		//NOOP
	}
}
