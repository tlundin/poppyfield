package com.teraime.poppyfield.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.CombinedRangeAndListFilter;
import com.teraime.poppyfield.base.Constants;
import com.teraime.poppyfield.base.FilterFactory;
import com.teraime.poppyfield.base.Rule;
import com.teraime.poppyfield.base.Spinners;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.base.Variable;
import com.teraime.poppyfield.room.VariableTable;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class WF_EntryField extends WF_Text_Widget {
    final LinearLayout innerInputContainer;
    final ScrollView scrollableInputContainer;
    final TextView valueDisplayField;
    final TextView unitDisplayField;
    final TextView headerInputCointainer;

    final Map<Variable, VariableView> myVars = new LinkedHashMap<>();
    private final Context ctx;
    private final boolean autoOpenSpinner;
    private final Map<Variable, String[]> values = new HashMap<>();


    private Variable keyVar;
    private ActionMode mActionMode;
    private String entryLabel;
    boolean iAmOpen = false;

    private Spinner firstSpinner = null;
    private static final Spinners sd = WorldViewModel.getStaticWorldRef().getSpinnerDefinitions();
    // Special behavior: If only a single boolean, don't open up the dialog.
    // Just set the value on click.
    private boolean singleBoolean = false;
    private String entryDescriptionText;
    private List<Rule> myRules;

    class VariableView {
        View view;
        boolean displayOut;
        String format;
        boolean isVisible;
        boolean showHistorical;
        String listTag;
        SpinnerAdapter adapter;
        String defautlValue;
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.tagpopmenu, menu);
            setBackgroundColor(R.color.pressed_color);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem x = menu.getItem(0);
            MenuItem y = menu.getItem(1);
            MenuItem z = menu.getItem(2);
            Log.d("nils", "myVars has " + myVars.size() + " elements. "
                    + myVars.toString());
            if (myVars.size() > 0) {

                z.setVisible(true);
                Variable.VariableConfiguration varC = keyVar.getVariableConfiguration();
                String url = varC.getUrl();

                if (url == null || url.length() == 0)
                    x.setVisible(false);
                else
                    x.setVisible(true);
                if ((varC.getVariableDescription() != null && varC.getVariableDescription().length() > 0 ) ||
                        (varC.getGroupDescription()!=null && varC.getGroupDescription().length()>0))
                    y.setVisible(true);
                else {
                    y.setVisible(false);
                    Log.d("burt",("vD: "+varC.getVariableDescription()));
                }

            } else {
                x.setVisible(false);
                y.setVisible(false);
                z.setVisible(false);
            }
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            Iterator<Variable> it = myVars.keySet().iterator();
            Variable v;
            v = it.next();
            Variable.VariableConfiguration varC = v.getVariableConfiguration();
            switch (item.getItemId()) {
                case R.id.menu_goto:

                    String url = varC.getUrl();
                    if (url!=null) {
                        Intent browse = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url));
                        browse.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        WorldViewModel.getStaticWorldRef().getActivity().startActivity(browse);
                    }

                    return true;
                case R.id.menu_delete:

                    if(innerInputContainer.getChildCount()==1) {
                        Log.d("boo","creating input fields!");
                        createInputFields();

                    }
                    for (Map.Entry<Variable, VariableView> pairs : myVars.entrySet()) {
                        Variable variable = pairs.getKey();
                        Log.d("vortex", "deleting variable " + variable.getId()
                                + " with value " + variable.getValue());
                        Variable.DataType type = variable.getType();
                        View view = pairs.getValue().view;

                        if (type == Variable.DataType.numeric || type == Variable.DataType.decimal
                                || type == Variable.DataType.text) {
                            EditText etview = view
                                    .findViewById(R.id.edit);
                            etview.setText("");
                        } else if (type == Variable.DataType.list) {
                            LinearLayout sl = (LinearLayout) view;
                            Spinner sp = sl.findViewById(R.id.spinner);
                            if (sp.getTag(R.string.u1) != null) {
                                TextView descr = sl.findViewById(R.id.extendedDescr);
                                descr.setText("");
                            }
                            sp.setSelection(-1);

                        } else if (type == Variable.DataType.bool) {
                            RadioGroup rbg = view
                                    .findViewById(R.id.radioG);
                            rbg.check(-1);
                        }

                    }
                    save();
                    refresh();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_info:
                    if (keyVar != null) {
                        Variable.VariableConfiguration al = keyVar.getVariableConfiguration();
                        StringBuilder msg =
                                new StringBuilder("Var_Label: " + al.getVarLabel() + "\n" +
                                        "Var_Desc : " + al.getVariableDescription() + "\n");
                        int i = 1;

                        while (al!=null)  {
                            msg.append(" Group_Lbl ").append(i).append(": ").append(al.getGroupLabel()).append("\n").append(" Group_Desc ").append(i).append(":  ").append(al.getGroupDescription()).append("\n");
                            i++;
                            al = (it.hasNext()?it.next().getVariableConfiguration():null);
                        }


                        new AlertDialog.Builder(ctx)
                                .setTitle(ctx.getResources().getString(R.string.description))
                                .setMessage(msg.toString())
                                .setPositiveButton(android.R.string.yes,
                                        (dialog, which) -> mode.finish())
                                .setIcon(android.R.drawable.ic_dialog_info).show();
                    }
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d("hox","ondestroy! for "+getLabel());
            mActionMode = null;
            revertBackgroundColor();
        }
    };


    public WF_EntryField(String id,
                         Context ctx,
                         String label,
                         String name,
                         String backgroundColor,
                         String text_color,
                         boolean isVisible,
                         boolean isAutoOpen,
                         int textSize,
                         int horizontalMargin,
                         int verticalMargin,
                         String vertical_format) {
        super(id, ctx, label, backgroundColor, "black", LayoutInflater.from(ctx).inflate(R.layout.selection_field_normal_horizontal, null), isVisible, textSize, horizontalMargin, verticalMargin);
        final LinearLayout outputContainer;
        LinearLayout displayLayout = (LinearLayout) LayoutInflater.from(ctx).inflate(getFieldLayout(), null);
        outputContainer = getWidget().findViewById(R.id.outputContainer);
        outputContainer.addView(displayLayout);
        scrollableInputContainer = (ScrollView) LayoutInflater.from(ctx).inflate(
                R.layout.input_container, null);
        innerInputContainer = (LinearLayout) scrollableInputContainer.findViewById(R.id.inner);
        headerInputCointainer = (TextView) innerInputContainer.findViewById(R.id.header);
        valueDisplayField = displayLayout.findViewById(R.id.outputValueField);
        unitDisplayField = displayLayout.findViewById(R.id.outputUnitField);
        autoOpenSpinner = isAutoOpen;
        this.ctx = ctx;

        getWidget().setClickable(true);

        getWidget().setOnClickListener(v -> {

            if (innerInputContainer.getChildCount() == 1) {
                Log.d("boo", "creating input fields!");
                createInputFields();

            } else {
                Log.d("boo", "not! creating input fields!");
            }
            setBackgroundColor(ctx.getColor(R.color.pressed_color));

            // special case. No dialog.

            if (singleBoolean) {
                Log.d("vortex", "singleboolean true..setting radio");
                VariableView vv = myVars.values().iterator().next();
                Variable var = myVars.keySet().iterator().next();
                String value = var.getValue();
                RadioButton ja = vv.view.findViewById(R.id.ja);
                RadioButton nej = vv.view.findViewById(R.id.nej);
                if (value == null || var.getValue().equals("false"))
                    ja.setChecked(true);
                else
                    nej.setChecked(true);
                save();
                refresh();
                //v.setBackgroundDrawable(originalBackground);
                revertBackgroundColor();
            } else {
                // On click, create dialog
                AlertDialog.Builder alert =
                        new AlertDialog.Builder(v.getContext());
                alert.setTitle(label);

                headerInputCointainer.setText(entryDescriptionText);
                refreshInputFields();
                iAmOpen = true;

                alert.setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                iAmOpen = false;
                                save();
                                refresh();
                                ViewGroup x = ((ViewGroup) scrollableInputContainer
                                        .getParent());
                                if (x != null)
                                    x.removeView(scrollableInputContainer);
                                revertBackgroundColor();
                                //v.setBackgroundDrawable(originalBackground);
                            }
                        });
                alert.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                iAmOpen = false;
                                ViewGroup x = ((ViewGroup) scrollableInputContainer
                                        .getParent());
                                if (x != null)
                                    x.removeView(scrollableInputContainer);
                                revertBackgroundColor();

                                //v.setBackgroundDrawable(originalBackground);
                            }
                        });
                if (scrollableInputContainer.getParent() != null)
                    ((ViewGroup) scrollableInputContainer.getParent())
                            .removeView(scrollableInputContainer);
                Dialog d = alert.setView(scrollableInputContainer).create();
                d.setCancelable(true);
                d.show();

            }
            // d.getWindow().setAttributes(lp);

        });


        getWidget().setOnLongClickListener(v -> {

            if (mActionMode != null) {
                return false;
            }


            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = ((Activity) ctx)
                    .startActionMode(mActionModeCallback);
            WF_EntryField.this.getWidget().setSelected(true);
            return true;

        });


    }

    private void refreshInputFields() {

        Variable.DataType numType;
        Log.d("nils", "In refreshinputfields");

        Set<Map.Entry<Variable, VariableView>> vars = myVars.entrySet();
        for (Map.Entry<Variable, VariableView> entry : vars) {
            Variable variable = entry.getKey();
            Variable.VariableConfiguration al = variable.getVariableConfiguration();
            String value = variable.getValue();
            Log.d("nils", "Variable: " + al.getVarLabel() + " value: "
                    + variable.getValue());
            numType = variable.getType();

            View v = entry.getValue().view;

            if (numType == Variable.DataType.bool) {
                RadioButton ja = v.findViewById(R.id.ja);
                RadioButton nej = v.findViewById(R.id.nej);
                if (value != null) {
                    if (value.equals("true"))
                        ja.setChecked(true);
                    else
                        nej.setChecked(true);
                }
            } else if (numType == Variable.DataType.numeric || numType == Variable.DataType.text) {

                // Log.d("nils","refreshing edittext with varid "+variable.getId());
                EditText et = v.findViewById(R.id.edit);
                String limitDesc = al.getLimitDescription();
                CombinedRangeAndListFilter filter = null;
                if (limitDesc!=null&&limitDesc.length()>0) {
                    filter = FilterFactory.getInstance(ctx).createLimitFilter(variable, limitDesc);
                    et.setFilters(new InputFilter[]{filter});
                }

                TextView limit = v.findViewById(R.id.limit);
                CharSequence limiTxt = new SpannableString("");
                et.setTextColor(Color.BLACK);
                if (variable.isUsingDefault()) {
                    et.setTextColor(ctx.getResources()
                            .getColor(R.color.purple,ctx.getTheme()));
                } else
                    Log.d("nils", "Variable " + variable.getId()
                            + " is NOT YELLOW");
                if (filter != null) {
                    if (variable.hasValueOutOfRange())
                        et.setTextColor(Color.RED);
                    limiTxt = TextUtils.concat(limiTxt, filter.prettyPrint());
                }
                et.setTextColor(Color.BLACK);
                /*
                 * CharSequence ruleExec =
                 * ruleExecutor.getRuleExecutionAsString(
                 * variable.getRuleState()); if (ruleExec!=null) { limiTxt =
                 * TextUtils.concat(limiTxt,ruleExec); if
                 * (variable.hasBrokenRules()) et.setTextColor(Color.RED); }
                 */
                limit.setText(limiTxt);
                et.setText(value == null ? "" : value);
                int position = et.getText().length();
                Selection.setSelection(et.getEditableText(), position);

            } else if (numType == Variable.DataType.list) {
                // this is the spinner.
                final Spinner sp = v.findViewById(R.id.spinner);

                final Handler h = new Handler();
                if (firstSpinner != null)
                    new Thread(new Runnable() {
                        public void run() {

                            h.postDelayed(new Runnable() {
                                public void run() {
                                    // Open the Spinner...
                                    if (firstSpinner.isShown())
                                        firstSpinner.performClick();
                                }
                            }, 500);
                        }
                    }).start();

                String[] opt = null;
                String tag = (String) sp.getTag(R.string.u1);
                Log.d("boo", "TAG IS " + tag);
                String val[] = values.get(variable);
                if (val != null) {

                    for (int i = 0; i < val.length; i++) {
                        if (val[i].equals(variable.getValue()))
                            sp.setSelection(i);
                    }
                } else if (tag != null && tag.equals("dynamic")) {
                    // Get the list values
                    LiveData<List<VariableTable>> optLiveData = Tools.requestDynamicList(variable);
                    optLiveData.observeForever(new Observer<List<VariableTable>>() {
                        @Override
                        public void onChanged(List<VariableTable> opto) {
                            List<String> values = new ArrayList<>();
                            for (VariableTable vt : opto)
                                values.add(vt.getValue());
                            Log.d("nils", "Got " + values.size() + " results");
                            //Remove duplicates and sort.
                            SortedSet<String> ss = new TreeSet<String>(new Comparator<String>() {
                                public int compare(String a, String b) {
                                    return Integer.parseInt(a) - Integer.parseInt(b);
                                }
                            }
                            );
                            String S;
                            for (int i = 0; i < values.size(); i++) {
                                S = values.get(i);
                                if (Tools.isNumeric(S))
                                    ss.add(S);
                                else
                                    Log.e("vortex", "NonNumeric value found: [" + S + "]");
                            }

                            String[] opt = new String[ss.size()];
                            int i = 0;
                            Iterator<String> it = ss.iterator();
                            while (it.hasNext()) {
                                opt[i++] = it.next();
                            }
                            ((ArrayAdapter<String>) sp.getAdapter()).clear();
                            ((ArrayAdapter<String>) sp.getAdapter()).addAll(opt);
                            String item = null;
                            if (sp.getAdapter().getCount() > 0) {
                                for (int j = 0; j  < sp.getAdapter().getCount(); j++) {
                                    item = (String) sp.getAdapter().getItem(j);
                                    if (item!=null && item.equals(value)) {
                                        sp.setSelection(j);
                                    }
                                }
                            }
                        }


                    });

                } else {
                    Log.e("vortex", "CONFIG ERROR");
                    opt = new String[]{"Config Error...please check your list definitions for variable " + variable.getLabel()};
                }
            } else if (numType == Variable.DataType.auto_increment) {
                EditText et = v.findViewById(R.id.edit);
                et.setText(value == null ? "0" : value);
            }
        }








    }



    public void addEntry(Variable v,
                         boolean isVisible,
                         boolean is_displayed,
                         String format,
                         boolean show_historical,
                         String default_value) {
        Log.d("WF_ENTRY","added variable "+v.getVariableConfiguration().getVarName()+" DEF VALUE "+default_value);
        if (myVars.isEmpty()) {
            keyVar = v;
            unitDisplayField.setText(v.getVariableConfiguration().getUnit());
            entryLabel = v.getVariableConfiguration().getEntryLabel();
            entryDescriptionText = v.getVariableConfiguration().getDescription();
        }

        VariableView vv = new VariableView();

        vv.isVisible=isVisible;
        vv.displayOut=is_displayed;
        vv.format=format;
        vv.showHistorical=show_historical;
        vv.listTag = null;
        vv.defautlValue = default_value;

        myVars.put(v,vv);


    }

    @Override
    public void refresh() {
        if (keyVar.getValue()==null) {
            String def = myVars.get(keyVar).defautlValue;
            if (def != null && !def.equals(Constants.NO_DEFAULT_VALUE)) {
                Log.d("WF_ENTRY","will use default");
                keyVar.setValueWithDefault(def);

            }
        }
        valueDisplayField.setText(keyVar.getValue()==null?"": keyVar.getValue());
    }


    public int getFieldLayout() {
        return R.layout.output_field_selection_element;
    }

    private
    android.graphics.drawable.Drawable originalBackground=null;

    private void setBackgroundColor(int color) {
        if (originalBackground==null)
            originalBackground = getWidget().getBackground();
        getWidget().setBackgroundColor(color);
    }


    //Check, and if required, create the inputfield elements.
    void createInputFields() {
        Log.d("vortex","in createInputFields");

        int vc=0;
        for (Variable var : myVars.keySet()) {
            VariableView varV = myVars.get(var);
            String unit = var.getVariableConfiguration().getUnit();
            String varLabel = var.getVariableConfiguration().getVarLabel();
            String varId = var.getId();
            String hist=null;


            if (varV.showHistorical) {
                hist = var.getHistValue();
                Log.d("vortex","historical fetched");

            }


            switch (var.getType()) {

                case bool:
                    // o.addRow("Adding boolean dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
                    View view = LayoutInflater.from(ctx).inflate(
                            R.layout.ja_nej_radiogroup, null);
                    TextView header = view.findViewById(R.id.header);

                    if (Tools.isNumeric(hist)) {
                        String histTxt = (hist.equals("true") ? ctx.getString(
                                R.string.yes) : ctx.getString(R.string.no));
                        SpannableString s = new SpannableString(varLabel + " ("
                                + histTxt + ")");
                        s.setSpan(new TextAppearanceSpan(ctx,
                                        R.style.PurpleStyle), varLabel.length() + 2,
                                s.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        header.setText(s);
                    } else
                        header.setText(varLabel);
                    innerInputContainer.addView(view);
                    varV.view=view;
                    break;
                case list:

                    LinearLayout sl = (LinearLayout) LayoutInflater.from(
                            ctx).inflate(
                            R.layout.edit_field_spinner, null);
                    final TextView sHeader = sl.findViewById(R.id.header);
                    final TextView sDescr = sl
                            .findViewById(R.id.extendedDescr);
                    final Spinner spinner = sl.findViewById(R.id.spinner);

                    spinner.setAdapter(varV.adapter);
                    innerInputContainer.addView(sl);
                    Log.d("nils", "Adding spinner for label " + entryLabel);

                    if (firstSpinner == null && vc==0 && autoOpenSpinner)
                        firstSpinner = spinner;
                    Log.d("boo","Setting tag to "+varV.listTag);
                    spinner.setTag(R.string.u1, varV.listTag);

                    varV.view=sl;

                    sHeader.setText(varLabel + (hist != null ? " (" + hist + ")" : ""));
                    //String listValues = al.getTable().getElement("List Values",
                    //        var.getBackingDataSet());


                    String[] opt =values.get(var);
                    if (opt != null && hist != null) {
                        try {
                            int histI = findSpinnerIndexFromValue(hist, opt);
                            if (histI < opt.length) {
                                String histT = opt[histI];

                                SpannableString s = new SpannableString(varLabel
                                        + " (" + histT + ")");
                                s.setSpan(new TextAppearanceSpan(ctx,
                                                R.style.PurpleStyle),
                                        varLabel.length() + 2, s.length() - 1,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                sHeader.setText(s);
                            }
                        } catch (NumberFormatException e) {
                            Log.d("vortex", "Hist spinner value is not a number: "
                                    + hist);
                        }
                    }

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView,
                                                   View selectedItemView, int position, long id) {
                            // Check if this spinner has side effects.
                            if (sd != null) {
                                String emsS = (String) spinner
                                        .getTag(R.string.u1);
                                List<Spinners.SpinnerElement> ems = null;
                                if (emsS != null)
                                    ems = sd.get(emsS);
                                @SuppressWarnings("unchecked")
                                List<String> curMapping = (List<String>) spinner
                                        .getTag(R.string.u2);
                                if (ems != null) {
                                    Spinners.SpinnerElement e = ems.get(position);
                                    Log.d("nils",
                                            "In onItemSelected. Spinner Element is "
                                                    + e.opt + " with variables "
                                                    + e.varMapping.toString());
                                    if (e.varMapping != null) {
                                        // hide the views for the last selected.
                                        hideOrShowViews(curMapping, false);
                                        hideOrShowViews(e.varMapping, true);
                                        spinner.setTag(R.string.u2, e.varMapping);
                                        sDescr.setText(e.descr);
                                        Log.d("nils", "DESCR TEXT SET TO " + e.descr);
                                    }
                                }
                            }
                        }

                        private void hideOrShowViews(List<String> varIds, boolean mode) {
                            Log.d("vortex", "In hideOrShowViews...");
                            if (varIds == null || varIds.size() == 0)
                                return;

                            for (String varId : varIds) {
                                Log.d("vortex", "Trying to find " + varId);
                                if (varId != null) {
                                    for (Variable v : myVars.keySet()) {
                                        Log.d("vortex", "Comparing with " + v.getId());
                                        if (v.getId().equalsIgnoreCase(varId.trim())) {
                                            Log.d("vortex", "Match! " + v.getId());
                                            View gView = myVars.get(v).view;
                                            gView.setVisibility(mode ? View.VISIBLE
                                                    : View.GONE);
                                            if (gView instanceof LinearLayout) {
                                                EditText et = gView
                                                        .findViewById(R.id.edit);
                                                if (et != null && !mode ) {
                                                    Log.e("nils",
                                                            "Setting view text to empty for "
                                                                    + v.getId());
                                                    et.setText("");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {

                        }

                    });

                    break;
                case text:
                    Log.d("vortex", "Adding text field for dy-variable with label "
                            + entryLabel + ", name " + varId + ", type "
                            + var.getType().name());
                    View l = LayoutInflater.from(ctx).inflate(
                            R.layout.edit_field_text, null);
                    header = l.findViewById(R.id.header);

                    header.setText(varLabel + " " + unit
                            + (hist != null ? " (" + hist + ")" : ""));
                    innerInputContainer.addView(l);
                    varV.view=l;
                    break;
                case numeric:
                case decimal:

                    // o.addRow("Adding edit field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
                    if (var.getType() == Variable.DataType.numeric) {
                        if (varV.format != null && varV.format.equals("slider")) {
                            l = LayoutInflater.from(ctx).inflate(
                                    R.layout.edit_field_slider, null);
                            SeekBar sb = l.findViewById(R.id.seekbar);
                            final EditText et = l.findViewById(R.id.edit);
                            et.setKeyListener(null);
                            String value = var.getValue();
                            //Initiate seekbar to variable value if any.
                            if (value != null)
                                sb.setProgress(Integer.parseInt(value));
                            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    et.setText(Integer.toString(progress));
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    Log.d("vortex", "hepp!");
                                }
                            });
                        } else {
                            l = LayoutInflater.from(ctx).inflate(
                                    R.layout.edit_field_numeric, null);
                        }
                    } else
                        l = LayoutInflater.from(ctx).inflate(
                                R.layout.edit_field_float, null);
                    header = l.findViewById(R.id.header);

                    String headerTxt = varLabel
                            + ((unit != null && unit.length() > 0) ? " (" + unit + ")"
                            : "");
                    if (hist != null && varV.showHistorical) {
                        SpannableString s = new SpannableString(headerTxt + " (" + hist
                                + ")");
                        s.setSpan(new TextAppearanceSpan(ctx,
                                R.style.PurpleStyle), headerTxt.length() + 2, s
                                .length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        header.setText(s);
                    } else
                        header.setText(headerTxt);

                    /*
                     * String limitDesc =
                     * al.getLimitDescription(var.getBackingDataSet()); if
                     * (limitDesc!=null&&limitDesc.length()>0) { EditText etNum =
                     * (EditText)l.findViewById(R.id.edit); CombinedRangeAndListFilter
                     * filter =
                     * FilterFactory.getInstance().createLimitFilter(var,limitDesc);
                     * etNum.setFilters(new InputFilter[] {filter}); }
                     */
                    // ruleExecutor.parseFormulas(al.getDynamicLimitExpression(var.getBackingDataSet()),var.getId());
                    innerInputContainer.addView(l);
                    varV.view=l;
                    break;
                case auto_increment:
                    Log.d("vortex", "Adding AUTO_INCREMENT variable " + varLabel);
                    l = LayoutInflater.from(ctx).inflate(
                            R.layout.edit_field_numeric, null);
                    header = l.findViewById(R.id.header);
                    header.setText(varLabel);
                    @SuppressLint("CutPasteId") EditText etNum = l.findViewById(R.id.edit);
                    etNum.setFocusable(false);
                    innerInputContainer.addView(l);
                    varV.view=l;
                    break;
            }
            if (!varV.isVisible)
                myVars.get(var).view.setVisibility(View.GONE);
            //next variable.
            vc++;
        }
    }

    private int findSpinnerIndexFromValue(String hist, String[] val) {
        int h = Integer.parseInt(hist);
        if (val == null)
            return h;
        int i = 0;
        for (String v : val) {
            if (Tools.isNumeric(v)) {
                if (hist.equals(v))
                    return i;
            }
            i++;
        }
        return h;
    }


    private void revertBackgroundColor() {
        if (originalBackground!=null) {
            getWidget().setBackgroundColor(Color.TRANSPARENT);
            getWidget().setBackground(originalBackground);
            originalBackground=null;
        } else
            getWidget().setBackgroundColor(Color.TRANSPARENT);

    }

    void save() {
        Log.d("boo","in save");
        boolean saveEvent = false;
        String newValue = null, existingValue = null;
        // for now only delytevariabler.
        Map<Variable, String> oldValue = new HashMap<Variable, String>();
        Iterator<Map.Entry<Variable, VariableView>> it = myVars.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Variable, VariableView> pairs = it
                    .next();
            Variable variable = pairs.getKey();
            existingValue = variable.getValue();
            oldValue.put(variable, existingValue);
            Variable.DataType type = variable.getType();
            View view = pairs.getValue().view;
            Log.d("boo", "Variable: "+variable.getLabel()+" Existing value: " + existingValue);
            if (type == Variable.DataType.bool) {
                // Get the yes radiobutton.
                RadioGroup rbg = view.findViewById(R.id.radioG);
                // If checked set value to True.
                int id = rbg.getCheckedRadioButtonId();

                if (id == R.id.nej) {
                    newValue = "false";
                } else if (id == R.id.ja) {
                    newValue = "true";
                } else
                    newValue = null;
            } else if (type == Variable.DataType.numeric || type == Variable.DataType.text
                    || type == Variable.DataType.decimal) {
                EditText etview = view.findViewById(R.id.edit);
                String txt = etview.getText().toString();
                if (txt.trim().length() > 0)
                    newValue = txt;
                else
                    newValue = null;
            } else if (type == Variable.DataType.list) {
                LinearLayout sl = (LinearLayout) view;
                Spinner sp = sl.findViewById(R.id.spinner);
                int s = sp.getSelectedItemPosition();
                String v[] = values.get(variable);
                if (v != null) {
                    if (s >= 0 && s < v.length)
                        newValue = v[s];
                    else
                        newValue = null;
                    Log.d("nils", "VALUE FOR SPINNER A " + newValue);
                } else {
                    newValue = (String) sp.getSelectedItem();
                    Log.d("nils", "VALUE FOR SPINNER B " + newValue);
                }
            } else if (type == Variable.DataType.auto_increment) {
                EditText etview = view.findViewById(R.id.edit);
                String s = etview.getText().toString();
                if (s != null && s.length() > 0) {
                    int val = Integer.parseInt(etview.getText().toString());
                    val++;
                    newValue = val + "";
                } else {
                    Log.e("vortex", "value is null or len 0 in auto_increment");
                    newValue = existingValue;
                }
            }

            if (newValue == null || !newValue.equals(existingValue)
                    || variable.isUsingDefault()) {
                Log.d("nils", "New value: " + newValue);
                saveEvent = true;

                if (newValue == null) {
                    Log.e("vortex", "Calling delete on " + variable.getId()
                            + "Obj:" + variable + " with keychain\n"
                            + variable.getContext().getColumnValues().toString());
                    variable.deleteValue();
                    Log.e("vortex",
                            "Getvalue now returns: " + variable.getValue());
                } else {
                    // Re-evaluate rules.
                    if (variable.hasValueOutOfRange()) {
                        saveEvent = false;
                        String earlierValue = variable.getValue();
                        if (earlierValue == null)
                            earlierValue = "";
                        Vibrator myVibrator = (Vibrator) ctx
                                .getSystemService(Context.VIBRATOR_SERVICE);

                        myVibrator.vibrate(VibrationEffect.createOneShot(250,VibrationEffect.DEFAULT_AMPLITUDE));
                        new AlertDialog.Builder(ctx)
                                .setTitle("Incorrect value")
                                .setMessage(
                                        "The value you entered is outside the allowed range. Earlier value will be used: ["
                                                + earlierValue + "]")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setCancelable(false)
                                .setNeutralButton("Ok",
                                        new Dialog.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                // TODO Auto-generated method
                                                // stub

                                            }
                                        }).show();
                    } else {
                        // check rules if value is in range.
                        variable.setValue(newValue);

                    }

                }

            } else {
                Log.d("nils", "New value was not set: " + newValue);
            }
        }

        Rule r = checkRules(keyVar.getContext());
        if (r != null) {
            saveEvent = false;
            Vibrator myVibrator = (Vibrator) ctx
                    .getSystemService(Context.VIBRATOR_SERVICE);
            myVibrator.vibrate(250);
            new AlertDialog.Builder(ctx).setTitle(r.getRuleHeader())
                    .setMessage(r.getRuleText())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .setNeutralButton("Ok", new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }

    }
    private Rule checkRules(com.teraime.poppyfield.base.Context mContext) {

        if (myRules == null)
            return null;
        Log.d("vortex", "In checkRules. I have " + myRules.size() + " rules");
        for (Rule r : myRules) {
            Log.d("vortex", " Rule: " + r.getCondition());

            Boolean res = r.execute(mContext);
            if (res != null && !res)
                return r;

        }
        return null;
    }
}
