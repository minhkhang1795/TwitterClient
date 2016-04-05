package com.codepath.apps.TwitterClient.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.widget.ImageView;

import com.codepath.apps.TwitterClient.R;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;

/**
 * Created by duyvu on 4/3/16.
 */
public class Utils {
    public static void inflateImage(final Context context, String url, final ImageView iv) {
        if (!url.equals("")) {
            Transformation transformation = new RoundedTransformationBuilder()
                    .cornerRadiusDp(4)
                    .oval(false)
                    .build();
            Picasso.with(context)
                    .load(url)
                    .transform(transformation)
                    .into(iv);
        }

        // I prefer using Picasso!!
//        Glide.with(context)
//                .load(url)
//                .asBitmap().into(new BitmapImageViewTarget(iv) {
//            @Override
//            protected void setResource(Bitmap resource) {
//                RoundedBitmapDrawable circularBitmapDrawable =
//                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
//                circularBitmapDrawable.setCornerRadius(6);
//                iv.setImageDrawable(circularBitmapDrawable);
//            }
//        });
    }

    public static void inflateImagePicasso(final Context context, String url, final ImageView iv) {
        Picasso.with(context)
                .load(url)
                .into(iv);
    }

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] {R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }
}
