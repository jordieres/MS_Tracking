package com.upm.jgp.healthywear.ui.main.fragments.socks;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.upm.jgp.healthywear.R;

/**
 * Created by zep on 01/08/17.
 */

public class SensoriaMultiEdit extends LinearLayoutCompat implements View.OnFocusChangeListener {
    private SensoriaEditText mCalibrationX;
    private SensoriaEditText mCalibrationY;
    private SensoriaEditText mCalibrationZ;
    private CharSequence[] mCalibrationValues;
    private String[] format;
    private int focusColor;
    private int unFocusColor;
    private MultiEditAction multiEditAction;
    private int iType;
    public boolean[] mandatory = {false, false, false};
    private Integer childFocus = null;
    private int iNextDown;
    private int iNextUp;
    private int iImeOptions;
    private float fTextSize;
    private boolean bEnabled;
    DisplayMetrics metrics;
    private int iTypeFace;
    private String[] name;

    public SensoriaMultiEdit(Context context) {
        super(context);
        initializeViews(context);
    }

    public SensoriaMultiEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        CharSequence[] arrayValues;

        TypedArray typedArray;
        typedArray = context
                .obtainStyledAttributes(attrs, R.styleable.SensoriaMultiEdit);
        arrayValues = typedArray
                .getTextArray(R.styleable.SensoriaMultiEdit_calibrationValues);
        mCalibrationValues = arrayValues;
        focusColor = typedArray.getColor(R.styleable.SensoriaMultiEdit_hasFocusColor, 0);
        unFocusColor = typedArray.getColor(R.styleable.SensoriaMultiEdit_hasNotFocusColor, 0);
        arrayValues = typedArray
                .getTextArray(R.styleable.SensoriaMultiEdit_mandatories);
        if (arrayValues != null) {
            mandatory[0] = Boolean.valueOf(arrayValues[0].toString());
            mandatory[1] = Boolean.valueOf(arrayValues[1].toString());
            mandatory[2] = Boolean.valueOf(arrayValues[2].toString());
        }
        arrayValues = typedArray
                .getTextArray(R.styleable.SensoriaMultiEdit_formats);
        if (arrayValues != null) {
            format = new String[]{
                    String.valueOf(arrayValues[0]),
                    String.valueOf(arrayValues[1]),
                    String.valueOf(arrayValues[2]),
            };
        }
        arrayValues = typedArray
                .getTextArray(R.styleable.SensoriaMultiEdit_names);
        if (arrayValues != null) {
            name = new String[]{
                    String.valueOf(arrayValues[0]),
                    String.valueOf(arrayValues[1]),
                    String.valueOf(arrayValues[2]),
            };
        }
        iType = typedArray.getInt(R.styleable.SensoriaMultiEdit_android_inputType,
                EditorInfo.TYPE_TEXT_VARIATION_NORMAL);

        iNextDown = typedArray.getResourceId(R.styleable.SensoriaMultiEdit_android_nextFocusDown, 0);
        iNextUp = typedArray.getResourceId(R.styleable.SensoriaMultiEdit_android_nextFocusUp, 0);
        iImeOptions = typedArray.getInt(R.styleable.SensoriaMultiEdit_android_imeOptions, EditorInfo.IME_ACTION_NEXT);
        metrics = context.getResources().getDisplayMetrics();
        fTextSize = (typedArray.getDimension(R.styleable.SensoriaMultiEdit_android_textSize,
                (14 * metrics.density)) / metrics.density);
        bEnabled = typedArray.getBoolean(R.styleable.SensoriaMultiEdit_android_enabled, false);
        iTypeFace = typedArray.getInt(R.styleable.SensoriaMultiEdit_android_textStyle, Typeface.NORMAL);
        typedArray.recycle();
        initializeViews(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setEnabled(false);
        setOnFocusChangeListener(this);
    }

    public SensoriaMultiEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context the current context for the view.
     */
    private void initializeViews(Context context) {

        mCalibrationX = new SensoriaEditText(context);
        mCalibrationX.setId(View.generateViewId());
        mCalibrationX.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        mCalibrationY = new SensoriaEditText(context);
        mCalibrationY.setId(View.generateViewId());

        mCalibrationY.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        mCalibrationZ = new SensoriaEditText(context);
        mCalibrationZ.setId(View.generateViewId());

        mCalibrationZ.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        FinishInflate();
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) 0.7);
        this.addView(mCalibrationX, params);
        this.addView(mCalibrationY, params);
        this.addView(mCalibrationZ, params);

    }

    public void setCalibrationValues(CharSequence[] calibrationValues) {
        switch (iType & InputType.TYPE_CLASS_NUMBER) {
            case InputType.TYPE_CLASS_NUMBER:
                mCalibrationX.setNumericText(Integer.parseInt(calibrationValues[0].toString()));
                mCalibrationY.setNumericText(Integer.parseInt(calibrationValues[1].toString()));
                mCalibrationZ.setNumericText(Integer.parseInt(calibrationValues[2].toString()));
                break;

            default:
                mCalibrationX.setAlphaText(calibrationValues[0].toString());
                mCalibrationY.setAlphaText(calibrationValues[1].toString());
                mCalibrationZ.setAlphaText(calibrationValues[2].toString());
                break;
        }
    }

    public void setCalibrationValues(Integer[] calibrationValues) {
        switch (iType & InputType.TYPE_CLASS_NUMBER) {
            case InputType.TYPE_CLASS_NUMBER:
                mCalibrationX.setNumericText(calibrationValues[0]);
                mCalibrationY.setNumericText(calibrationValues[1]);
                mCalibrationZ.setNumericText(calibrationValues[2]);
                break;

            default:
                mCalibrationX.setAlphaText(calibrationValues[0].toString());
                mCalibrationY.setAlphaText(calibrationValues[1].toString());
                mCalibrationZ.setAlphaText(calibrationValues[2].toString());
                break;
        }
    }

    public void setCalibrationValues(Short[] calibrationValues) {
        setCalibrationValues(new Integer[]{
                (int) calibrationValues[0],
                (int) calibrationValues[1],
                (int) calibrationValues[2]
        });
    }

    public int getFocusColor() {
        return focusColor;
    }

    public void setFocusColor(int fc) {
        focusColor = fc;
        mCalibrationX.setFocusColor(fc);
        mCalibrationY.setFocusColor(fc);
        mCalibrationZ.setFocusColor(fc);
    }

    public int getIdleColor() {
        return unFocusColor;
    }

    public void setIdleColor(int ic) {
        unFocusColor = ic;
        mCalibrationX.setIdleColor(ic);
        mCalibrationY.setIdleColor(ic);
        mCalibrationZ.setIdleColor(ic);
    }

    public boolean[] getMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean[] m) {
        mandatory = m;

        mCalibrationX.setMandatory(false);
        mCalibrationY.setMandatory(false);
        mCalibrationZ.setMandatory(false);
    }

    public String[] getFormat() {
        return format;
    }

    public void setFormat(String[] f) {
        format = f;
        if (!format[0].isEmpty())
            mCalibrationX.setFormat(f[0]);
        if (!format[1].isEmpty())
            mCalibrationY.setFormat(f[1]);
        if (!format[2].isEmpty())
            mCalibrationZ.setFormat(f[2]);
    }


    public CharSequence[] getAlphaText() {
        try {
            return new CharSequence[]{
                    mCalibrationX.getAlphaText(),
                    mCalibrationY.getAlphaText(),
                    mCalibrationZ.getAlphaText()
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Number[] getNumericText() {
        try {
            return new Number[]{
                    mCalibrationX.getNumericText(),
                    mCalibrationY.getNumericText(),
                    mCalibrationZ.getNumericText()
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus())
            if (iImeOptions == EditorInfo.IME_ACTION_NEXT) {
                if (childFocus == null)
                    mCalibrationX.requestFocus();
                else if (childFocus == mCalibrationX.getId()) {
                    mCalibrationY.requestFocus();
                }
            } else if (iImeOptions == EditorInfo.IME_ACTION_PREVIOUS) {
                if (childFocus == null)
                    mCalibrationZ.requestFocus();
                else if (childFocus == mCalibrationX.getId()) {
                    mCalibrationY.requestFocus();
                }
            }
    }

    @Override
    public void setEnabled(boolean e) {
        super.setEnabled(e);
        mCalibrationX.setEnabled(e);
        mCalibrationY.setEnabled(e);
        mCalibrationZ.setEnabled(e);
    }

    public int getInputType() {
        return iType;
    }

    public void setInputType(int i) {
        iType = i;
        mCalibrationX.setInputType(iType);
        mCalibrationY.setInputType(iType);
        mCalibrationZ.setInputType(iType);
    }

    @Override
    public int getNextFocusDownId() {
        return iNextDown;
    }

    @Override
    public void setNextFocusDownId(int index) {
        iNextDown = index;
        mCalibrationZ.setNextFocusDownId(iNextDown);
    }

    @Override
    public int getNextFocusUpId() {
        return iNextUp;
    }

    @Override
    public void setNextFocusUpId(int index) {
        iNextUp = index;
        mCalibrationX.setNextFocusDownId(iNextUp);
    }

    public int getStyle() {
        return iTypeFace;
    }

    public void setStyle(int style) {
        iTypeFace = style;
        mCalibrationX.setStyle(style);
        mCalibrationY.setStyle(style);
        mCalibrationZ.setStyle(style);
    }

    public int getImeOptions() {
        return iImeOptions;
    }

    public void setImeOptions(int iio) {
        iImeOptions = iio;
        mCalibrationX.setImeOptions(iio);
        mCalibrationY.setImeOptions(iio);
        mCalibrationZ.setImeOptions(iio);
    }

    public float getTextSize() {
        return fTextSize;
    }

    public void setTextSize(float ts) {
        fTextSize = ts;
        mCalibrationX.setTextSize(ts + 1);
        mCalibrationY.setTextSize(ts + 1);
        mCalibrationZ.setTextSize(ts + 1);
    }

    private void setIgnoreTextChanged() {
        mCalibrationX.setIgnoreTextChanged();
        mCalibrationY.setIgnoreTextChanged();
        mCalibrationZ.setIgnoreTextChanged();
    }

    public interface MultiEditAction {
        void onMultiEdit(View v);
    }

    public void setOnEditAction(MultiEditAction editAction) {
        this.multiEditAction = editAction;
    }

    private boolean canExecuteCallback() {
        if (!mCalibrationX.hasFocus() && !mCalibrationY.hasFocus() && !mCalibrationZ.hasFocus()) {
            if ((mandatory[0] & mCalibrationX.isEmpty()) ||
                    (mandatory[1] & mCalibrationY.isEmpty()) ||
                    (mandatory[2] & mCalibrationZ.isEmpty()))
                return false;
            else return true;
        }
        return false;
    }

    void FinishInflate() {

        // Sets the images for the previous and next buttons. Uses
        // built-in images so you don't need to add images, but in
        // a real application your images should be in the
        // application package so they are always available.

        mCalibrationX.setOnEditAction(new SensoriaEditText.EditAction() {
            @Override
            public void onEdit(View v) {
                if (canExecuteCallback())
                    multiEditAction.onMultiEdit(v);
                else {
                    if (mCalibrationX.wasFocused())
                        mCalibrationX.setTextWithoutChange();
                    if (mCalibrationY.wasFocused())
                        mCalibrationY.setTextWithoutChange();
                    else if (mCalibrationZ.wasFocused())
                        mCalibrationZ.setTextWithoutChange();
                }
            }
        });
        mCalibrationX.setOnChildFocus(new SensoriaEditText.ChildFocus() {
            @Override
            public void onChildFocus() {
                if (iImeOptions == EditorInfo.IME_ACTION_NEXT)
                    childFocus = mCalibrationX.getId();
                else if (iImeOptions == EditorInfo.IME_ACTION_PREVIOUS)
                    childFocus = null;
            }
        });
        mCalibrationX.setImeOptions(iImeOptions);
        if (iNextUp != 0)
            mCalibrationX.setNextFocusUpId(iNextUp);
        mCalibrationX.setTextSize(fTextSize);
        mCalibrationX.setEnabled(bEnabled);

        mCalibrationY.setOnEditAction(new SensoriaEditText.EditAction() {
            @Override
            public void onEdit(View v) {
                if (canExecuteCallback())
                    multiEditAction.onMultiEdit(v);
                else {
                    if (mCalibrationX.wasFocused())
                        mCalibrationX.setTextWithoutChange();
                    if (mCalibrationY.wasFocused())
                        mCalibrationY.setTextWithoutChange();
                    else if (mCalibrationZ.wasFocused())
                        mCalibrationZ.setTextWithoutChange();
                }
            }
        });
        mCalibrationY.setOnChildFocus(new SensoriaEditText.ChildFocus() {
            @Override
            public void onChildFocus() {
                childFocus = null;
            }
        });
        mCalibrationY.setImeOptions(iImeOptions);
        mCalibrationX.setNextFocusDownId(mCalibrationY.getId());
        mCalibrationY.setNextFocusUpId(mCalibrationX.getId());
        mCalibrationY.setTextSize(fTextSize);
        mCalibrationY.setEnabled(bEnabled);

        mCalibrationZ.setOnEditAction(new SensoriaEditText.EditAction() {
            @Override
            public void onEdit(View v) {
                if (canExecuteCallback())
                    multiEditAction.onMultiEdit(v);
                else {
                    if (mCalibrationX.wasFocused())
                        mCalibrationX.setTextWithoutChange();
                    if (mCalibrationY.wasFocused())
                        mCalibrationY.setTextWithoutChange();
                    else if (mCalibrationZ.wasFocused())
                        mCalibrationZ.setTextWithoutChange();
                }
            }
        });
        mCalibrationZ.setOnChildFocus(new SensoriaEditText.ChildFocus() {
            @Override
            public void onChildFocus() {
                if (iImeOptions == EditorInfo.IME_ACTION_NEXT)
                    childFocus = null;
                else if (iImeOptions == EditorInfo.IME_ACTION_PREVIOUS)
                    childFocus = mCalibrationZ.getId();
            }
        });
        mCalibrationZ.setImeOptions(iImeOptions);
        if (iNextDown != 0)
            mCalibrationZ.setNextFocusDownId(iNextDown);
        mCalibrationY.setNextFocusDownId(mCalibrationZ.getId());
        mCalibrationZ.setNextFocusUpId(mCalibrationY.getId());
        mCalibrationZ.setTextSize(fTextSize);
        mCalibrationZ.setEnabled(bEnabled);

        if (mandatory != null)
            setMandatory(mandatory);
        if (format != null)
            setFormat(format);
        if (focusColor != 0)
            setFocusColor(focusColor);
        if (unFocusColor != 0)
            setIdleColor(unFocusColor);
        setInputType(iType);
        if (mCalibrationValues != null)
            setCalibrationValues(mCalibrationValues);
        setStyle(iTypeFace);
        setIgnoreTextChanged();
    }
}
