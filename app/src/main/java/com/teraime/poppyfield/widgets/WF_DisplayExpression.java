package com.teraime.poppyfield.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Expressor;

import java.util.List;

public class WF_DisplayExpression extends WF_Text_Widget {

    final TextView valueDisplayField;
    final TextView unitDisplayField;
    final List<Expressor.EvalExpr> exprE;
    final com.teraime.poppyfield.base.Context mContext;

    public WF_DisplayExpression(String id, Context androidCtx, com.teraime.poppyfield.base.Context mContext,String label, String expression, String unit, String backgroundColor, String text_color, boolean isVisible, int textSize, int horisontalMargin, int verticalMargin) {
        super(id, androidCtx,label,backgroundColor,"black", LayoutInflater.from(androidCtx).inflate(R.layout.display_value_textview_horizontal,null), isVisible,textSize, horisontalMargin, verticalMargin);
        exprE = Expressor.preCompileExpression(expression);
        valueDisplayField = getWidget().findViewById(R.id.outputValueField);
        unitDisplayField = getWidget().findViewById(R.id.outputUnitField);
        unitDisplayField.setText(unit);
        this.mContext = mContext;
    }


    @Override
    public void refresh() {
        valueDisplayField.setText(Expressor.analyze(exprE,mContext));
    }
}
