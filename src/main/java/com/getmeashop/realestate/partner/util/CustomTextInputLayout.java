package com.getmeashop.realestate.partner.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

/**
 * Created by nikka on 12/8/15.
 */
public class CustomTextInputLayout extends TextInputLayout {

    public CustomTextInputLayout(Context context) {
        super(context);
    }

    public CustomTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            // TextInputLayout updates mCollapsingTextHelper bounds on onLayout. but Edit text is not layouted.
            child.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("WrongCall")
                @Override
                public void onGlobalLayout() {
                    onLayout(false, getLeft(), getTop(), getRight(), getBottom());
                }
            });
        }
        super.addView(child, index, params);
    }
}
