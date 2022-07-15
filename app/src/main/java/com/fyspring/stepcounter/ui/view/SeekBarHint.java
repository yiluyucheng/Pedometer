package com.fyspring.stepcounter.ui.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.fyspring.stepcounter.R;

@SuppressLint("AppCompatCustomView")
public class SeekBarHint extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    private int mPopupWidth;
    private int mPopupStyle;
    public static final int POPUP_FOLLOW = 0;
    private int mYLocationOffset;
    private int leftText = 0;
    private int rightText = 0;
    private int progressText = 0;
    private int step;
    private OnSeekBarChangeListener mInternalListener;
    private OnSeekBarChangeListener mExternalListener;

    private OnSeekBarHintProgressChangeListener mProgressChangeListener;

    public interface OnSeekBarHintProgressChangeListener {
        public String onHintTextChanged(SeekBarHint seekBarHint, int progress);
    }

    public SeekBarHint(Context context) {
        super(context);
        init(context, null);
    }

    public SeekBarHint(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SeekBarHint(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        setOnSeekBarChangeListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarHint);

        mPopupWidth = (int) a.getDimension(R.styleable.SeekBarHint_popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        mYLocationOffset = (int) a.getDimension(R.styleable.SeekBarHint_yOffset, 0);
        mPopupStyle = a.getInt(R.styleable.SeekBarHint_popupStyle, POPUP_FOLLOW);

        a.recycle();


    }
    public void setLeftText(int str) {
        this.leftText = str;
    }

    public void setRightText(int str) {
        this.rightText = str;
    }

    public void setProgressText(int str) {
        this.progressText = str;
    }

    public void initShow() {

        this.setMax((int) (rightText - leftText) * 10);
        this.setProgress((int) ((progressText - leftText) * 10));
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        if (mInternalListener == null) {
            mInternalListener = l;
            super.setOnSeekBarChangeListener(l);
        } else {
            mExternalListener = l;
        }
    }

    public void setOnProgressChangeListener(OnSeekBarHintProgressChangeListener l) {
        mProgressChangeListener = l;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        String popupText = null;
        if (mProgressChangeListener != null) {
            popupText = mProgressChangeListener.onHintTextChanged(this, cuclaProcess(leftText));
        }

        if (mExternalListener != null) {
            mExternalListener.onProgressChanged(seekBar, progress, b);
        }

        step = cuclaProcess(leftText);

    }

    public int cuclaProcess(int left) {
        return (leftText * 10 + getProgress()) / 10;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mExternalListener != null) {
            mExternalListener.onStartTrackingTouch(seekBar);
        }

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mExternalListener != null) {
            mExternalListener.onStopTrackingTouch(seekBar);
        }

    }

}
