package com.DorelSaig.superme.Misc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.DorelSaig.superme.UploadIMGListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Utils {

    /**
     * The Following function will get an image data as URI convert it to bytes and will store those bytes in the Firebase Storage,
     * when done, will callback to the listener and send to him the url of the image in the storage.
     *
     * @param uploadIMGListener Listener to which the result will be sent to when method is done
     * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     * @param imageView         An ImageView (ShapeableImageView) that will hold the uploaded image and we use it to get the data as bytes.
     * @param storageRef        A StorageReference to the exact path where we want the image to be store in Storage
     * @param context           The context to use. Usually your android.app.Application or android.app.Activity object
     */
    public static void imageUploading(UploadIMGListener uploadIMGListener, Intent data, ShapeableImageView imageView, StorageReference storageRef, Context context) {
        Uri uri = data.getData();
        imageView.setImageURI(uri);

        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        //Start The upload task
        UploadTask uploadTask = storageRef.putBytes(bytes);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // If upload was successful, We want to get the image url from the storage
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            uploadIMGListener.uploadDone(uri.toString());
                        }
                    });
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Function will Create basic Red Snackbar message
     *
     * @param view     The view to find a parent from
     * @param text     The text to show. Can be formatted text
     * @param duration The text to show. Can be formatted text.
     *                 duration – How long to display the message. Can be LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE, or a custom duration in milliseconds.
     * @return The Snackbar with some theme settings attached to it. like background and text color (Error Theme) and Slide Animation
     */
    public static Snackbar showErrorSnackBar(@NonNull android.view.View view,
                                             @NonNull CharSequence text,
                                             int duration) {
        return Snackbar
                .make(view, text, duration)
                .setBackgroundTint(Color.parseColor("#F9E1DC"))
                .setActionTextColor(Color.RED)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
    }

    /**
     * Slide the view from below itself to the current position
     *
     * @param view the view which we want to slide
     */
    public static void slideUp(View view) {
        //view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    /**
     * Slide the view from the current position to below itself
     *
     * @param view the view which we want to slide
     */
    public static void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillEnabled(false);
        view.startAnimation(animate);

    }
}
