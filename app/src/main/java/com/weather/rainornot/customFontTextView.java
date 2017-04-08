package com.weather.rainornot;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

/**
 * Created by pramodbhadana on 08/04/17.
 */

public class customFontTextView  extends AppCompatTextView{
    public customFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public customFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public customFontTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf");
            setTypeface(tf);
        }
        setTextSize(20);
    }
}
