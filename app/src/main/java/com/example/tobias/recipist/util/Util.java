package com.example.tobias.recipist.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.tobias.recipist.R;

/**
 * Created by Tobias on 25-06-2016.
 */
public class Util {
    public static final int TOOLBAR_NAVIGATION_ICON_CLICK_ID = 16908332;

    public static final int DRAWABLE_POSITION_LEFT = 0;
    public static final int DRAWABLE_POSITION_TOP = 1;
    public static final int DRAWABLE_POSITION_RIGHT = 2;
    public static final int DRAWABLE_POSITION_BOTTOM = 3;

    public static EditText setupEditText(Context context, ViewGroup.LayoutParams layoutParams, String text, int maxLines, int inputType, String hint) {
        EditText editText = new EditText(context);
        editText.setLayoutParams(layoutParams);
        editText.setText(text);
        editText.setMaxLines(maxLines);
        editText.setInputType(inputType);
        editText.setHint(hint);
        return editText;
    }

    public static void addView(ViewGroup parent, View child) {
        parent.addView(child);
    }

    public static void addDrawableToTheRight(EditText editText, Drawable drawable) {
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public static void makeChildDeletableByClickingOnRightDrawalbe(final ViewGroup parent, final EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_POSITION_RIGHT].getBounds().width())) {
                        parent.removeView(editText);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }
}