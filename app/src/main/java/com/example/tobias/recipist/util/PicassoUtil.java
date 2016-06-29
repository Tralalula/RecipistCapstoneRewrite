package com.example.tobias.recipist.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.tobias.recipist.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by Tobias on 29-06-2016.
 */
public class PicassoUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Picasso.with(context)
                .load(url)
                .placeholder(context.getDrawable(R.drawable.chocfond))
                .error(context.getDrawable(R.drawable.error))
                .into(imageView);
    }

    public static Bitmap getBitmap(Context context, String url) throws IOException {
        return Picasso.with(context)
                .load(url)
                .placeholder(context.getDrawable(R.drawable.chocfond))
                .error(context.getDrawable(R.drawable.error))
                .get();
    }
}
