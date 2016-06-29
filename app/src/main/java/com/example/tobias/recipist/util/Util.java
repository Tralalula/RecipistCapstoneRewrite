package com.example.tobias.recipist.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tobias.recipist.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tobias on 25-06-2016.
 */
public class Util {
    public static final String TAG = Util.class.getSimpleName();

    public static final int TOOLBAR_NAVIGATION_ICON_CLICK_ID = 16908332;

    public static final int DRAWABLE_POSITION_LEFT = 0;
    public static final int DRAWABLE_POSITION_TOP = 1;
    public static final int DRAWABLE_POSITION_RIGHT = 2;
    public static final int DRAWABLE_POSITION_BOTTOM = 3;

    public static EditText setupEditText(Context context, ViewGroup.LayoutParams layoutParams, String text, int maxLines, int inputType, String hint, int gravity) {
        EditText editText = new EditText(context);
        editText.setLayoutParams(layoutParams);
        editText.setText(text);
        editText.setGravity(gravity);
        editText.setMaxLines(maxLines);
        editText.setInputType(inputType);
        editText.setHint(hint);
        return editText;
    }

    public static TextView setupTextView(Context context, ViewGroup.LayoutParams layoutParams, String text, int textColor) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        textView.setGravity(Gravity.START);
        textView.setTextColor(textColor);

        return textView;
    }

    public static TextView setupTextView(Context context, ViewGroup.LayoutParams layoutParams, Spanned text, int textColor) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        textView.setGravity(Gravity.START);
        textView.setTextColor(textColor);

        return textView;
    }

    public static void addView(ViewGroup parent, View child) {
        parent.addView(child);
    }

    public static void addDrawableToTheRight(EditText editText, Drawable drawable) {
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public static void addDrawableToTheLeft(EditText editText, Drawable drawable) {
        editText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    public static void makeChildDeletableByClickingOnRightDrawable(final ViewGroup parent, final EditText editText) {
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

    public static void makeChildDeletableByClickingOnLeftDrawable(final ViewGroup parent, final EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() <= editText.getCompoundDrawables()[DRAWABLE_POSITION_LEFT].getBounds().width() * 2) {
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

    public static boolean isNullOrEmpty(String string) {
        return (string == null || TextUtils.isEmpty(string));
    }

    public static String formatTime(int timeInMinutes) {
        int days = timeInMinutes / 1440;
        timeInMinutes -= (days * 1440);
        int hours = timeInMinutes / 60;
        int minutes = timeInMinutes % 60;

        String formattedTime = "";
        if (days > 0) formattedTime += days + "d";
        if (hours > 0) formattedTime += hours + "h";
        if (minutes > 0) formattedTime += minutes + "m";

        return formattedTime;
    }
}
