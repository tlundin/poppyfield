package com.teraime.poppyfield.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import com.teraime.poppyfield.R;

public class WF_EntryField extends WF_Text_Widget {


    @SuppressWarnings("WrongConstant")
	public WF_EntryField(String id, Context ctx, String label, String backgroundColor, boolean isVisible, boolean isAutoOpen,int textSize, int horizontalMargin, int verticalMargin) {
		super(id,ctx,label,backgroundColor,"black", LayoutInflater.from(ctx).inflate(R.layout.selection_field_normal_horizontal,null), isVisible,textSize,horizontalMargin,verticalMargin);

    }


}
