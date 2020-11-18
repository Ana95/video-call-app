package com.example.videoconferenceapp.helperFunctions;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ImagesHelperFunctions {

    public static void loadImage(final ImageView imageView, String key, Context context){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("userImages");
        storageReference.child(key + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                loadImageToImageView(uri, imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("error", e.getMessage());
            }
        });
    }

    public static void loadImageToImageView(Uri uri, ImageView imageView) {
        Picasso.get()
                .load(uri.toString())
                .fit()
                .centerCrop()
                .into(imageView);
    }
}
