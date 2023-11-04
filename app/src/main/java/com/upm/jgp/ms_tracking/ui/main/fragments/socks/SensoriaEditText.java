package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.upm.jgp.healthywear.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zep on 29/07/17.
 */

public class SensoriaEditText extends AppCompatEditText
        implements
        View.OnFocusChangeListener,
        TextWatcher,
        EditText.OnEditorActionListener {
    private volatile boolean textChanged = false;
    private TextWatcher tw;
    private EditAction editAction;
    private int focusColor = this.getCurrentTextColor();
    private int idleColor = this.getCurrentTextColor();
    private boolean mandatory = false;
    private String format = "%s";
    private ChildFocus childFocus = null;
    private CharSequence beforeChange;
    private int inputType;
    private int iTypeFace;
    private boolean hadFocus = false;
    private boolean ignoreTextChanged = false;

    public SensoriaEditText(Context context) {
        super(context);
        initializeViews(null);
    }

    public SensoriaEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray;

        typedArray = context
                .obtainStyledAttributes(attrs, R.styleable.SensoriaEditText);
        initializeViews(typedArray);
        typedArray.recycle();
    }

    public SensoriaEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray;

        typedArray = context
                .obtainStyledAttributes(attrs, R.styleable.SensoriaEditText);
        initializeViews(typedArray);
        typedArray.recycle();
    }

    private void initializeViews(TypedArray typedArray) {
        if (typedArray != null) {
            format = typedArray
                    .getString(R.styleable.SensoriaEditText_format);
            mandatory = typedArray.getBoolean(R.styleable.SensoriaEditText_mandatory, false);
            focusColor = typedArray.getColor(R.styleable.SensoriaEditText_focusColor, this.getCurrentTextColor());
            idleColor = typedArray.getColor(R.styleable.SensoriaEditText_idleColor, this.getCurrentTextColor());
            inputType = typedArray.getInt(R.styleable.SensoriaEditText_android_inputType,
                    EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
            iTypeFace = typedArray.getInt(R.styleable.SensoriaEditText_android_textStyle, Typeface.NORMAL);
            super.setInputType(inputType);
        }
        setOnTextChangeListener(this);
        setOnFocusChangeListener(this);
        setOnEditorActionListener(this);
    }

    public void setFocusColor(int color) {
        focusColor = color;
    }

    public void setIgnoreTextChanged() {
        ignoreTextChanged = true;
    }

    public void setIdleColor(int color) {
        idleColor = color;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setStyle(int typeface) {
        iTypeFace = typeface;
        setTypeface(null, iTypeFace);
    }

    public void setOnTextChangeListener(TextWatcher tw) {
        try {
            removeTextChangedListener(this.tw);
        } catch (Throwable e) {
        }
        addTextChangedListener(tw);
        this.tw = tw;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            editAction.onEdit(this);
            return true;
        }

        return false;
    }

    public interface EditAction {
        void onEdit(View v);
    }

    interface ChildFocus {
        void onChildFocus();
    }

    public void setOnEditAction(EditAction editAction) {
        this.editAction = editAction;
    }

    public void setOnChildFocus(ChildFocus childFocus) {
        this.childFocus = childFocus;
    }

    public void setTextWithoutFocus(String sText) {
        super.setInputType(InputType.TYPE_CLASS_TEXT);
        this.setTextColor(idleColor);
        this.setText(sText);
        this.setTypeface(null, iTypeFace);
        textChanged = false;
    }

    public void setTextWithoutFocus(int nText) {
        super.setInputType(InputType.TYPE_CLASS_TEXT);
        this.setTextColor(idleColor);
        this.setText(String.format(format, nText));
        this.setTypeface(null, iTypeFace);
        textChanged = false;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(getAlphaText());
    }

    public void setAlphaText(CharSequence txt) {
        setAlphaText(txt.toString());
    }

    public void setAlphaText(String sText) {
        if (hasFocus())
            this.setText(String.format("%s", sText));
        else
            this.setTextWithoutFocus(sText);
        textChanged = false;
    }

    public void setNumericText(Number nText) {
        if (hasFocus())
            this.setText(String.format("%d", nText));
        else
            this.setTextWithoutFocus(nText.intValue());
        textChanged = false;
    }

    public CharSequence getAlphaText() {
        if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
            Number n = getNumber();
            if (n != null) return n.toString();
            else return null;
        } else return getText();
    }

    public CharSequence getAlphaText(CharSequence cs) {
        if (!TextUtils.isEmpty(cs)) {
            if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
                Number n = getNumber(cs);
                if (n != null) return n.toString();
                else return null;
            }
        }
        return cs;
    }


    public Number getNumericText() {
        Number n = getNumber();
        if (n == null)
            if (mandatory)
                throw new IllegalArgumentException("The parameter is mandatory");
        return n;
    }

    Number getNumber() {
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(this.getText());
        if (m.find()) {
            Long value = Long.valueOf(m.group());
            Long maxValue = abs(value);
            if (maxValue <= Short.MAX_VALUE)
                return value.shortValue();
            else if (maxValue <= Integer.MAX_VALUE)
                return value.intValue();
            else
                return value;
        } else return null;
    }

    Number getNumber(CharSequence cs) {
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(cs);
        if (m.find()) {
            Long maxValue = abs(Long.valueOf(m.group()));
            if (maxValue <= Short.MAX_VALUE)
                return maxValue.shortValue();
            else if (maxValue <= Integer.MAX_VALUE)
                return maxValue.intValue();
            else
                return maxValue;
        } else return null;
    }

    @Override
    public void setInputType(int inputType) {
        this.inputType = inputType;
        super.setInputType(inputType);
    }

    public void setTextWithoutChange() {
        if (!TextUtils.isEmpty(getText()))
            if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER)
                setTextWithoutFocus(getNumber().intValue());
            else
                setTextWithoutFocus(getAlphaText().toString());
    }

    public boolean wasFocused() {
        if (hadFocus) {
            hadFocus = false;
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            hadFocus = true;
            if ((textChanged | ignoreTextChanged) && (!mandatory || !TextUtils.isEmpty(getText()))) {
                textChanged = false;
                editAction.onEdit(this);
            } else setTextWithoutChange();
        } else {
            if (childFocus != null)
                childFocus.onChildFocus();
            if (!TextUtils.isEmpty(getText())) {
                setText(String.format("%s", getAlphaText()));
            }
            super.setInputType(inputType);
            setTextColor(focusColor);
            setTypeface(null, Typeface.NORMAL);
            textChanged = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (getText().length() != 0 && !textChanged)
            beforeChange = getAlphaText(s);

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        CharSequence afterChange;
        if (getText().length() != 0 && !textChanged) {
            afterChange = getAlphaText();
            if (!TextUtils.isEmpty(afterChange)) {
                if (!TextUtils.equals(beforeChange, afterChange))
                    textChanged = true;
            } else {
                if (!TextUtils.isEmpty(beforeChange))
                    textChanged = true;
            }
        }
    }
}
