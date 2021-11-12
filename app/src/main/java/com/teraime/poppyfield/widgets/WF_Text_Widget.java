package com.teraime.poppyfield.widgets;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Tools;

public abstract class WF_Text_Widget extends WF_Widget {
    public WF_Text_Widget(String id, Context ctx, String label, String backgroundColor, String textColor, View v, boolean isVisible, int textSize, int horizontalMargin, int verticalMargin) {
        super(id,label,v,isVisible);
        if (label!=null) {
            Log.d("vortex","Label is: "+label);
            Log.d("bortex","Margins: "+horizontalMargin+","+verticalMargin+" t: "+textSize);
            TextView tv = getWidget().findViewById(R.id.text_block);
            tv.setText(Html.fromHtml(label));
            if (textSize!=-1)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)tv.getLayoutParams();
            lp.setMargins(horizontalMargin,verticalMargin,horizontalMargin,verticalMargin);
        }
        if (backgroundColor!=null)
            getWidget().setBackgroundColor(Tools.getColorResource(ctx,backgroundColor));
    }


}
