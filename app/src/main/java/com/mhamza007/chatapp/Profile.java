package com.mhamza007.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    FirebaseAuth auth;
    String currentUserId;
    DatabaseReference db;

    HashMap<String, Object> userMap = new HashMap<>();
    int RC_SELECT_IMAGE = 4;
    Uri imageUri = null;

    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance().getReference();

        profileImage = findViewById(R.id.profile_image);

        retreiveUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder profileAlert = new AlertDialog.Builder(Profile.this);
                profileAlert
                        .setTitle("Change Image?")
                        .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeProfileImage();
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                profileAlert.show();
            }
        });
    }

    private void retreiveUserInfo() {
        db.child("User").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("image")){
                        String pic = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(pic).into(profileImage);
                    } else {
                        Picasso.get().load(R.mipmap.ic_launcher_round).into(profileImage);
                    }
                } else {
                    Log.e("Profile", "Data Snapshot doesn't exists...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void changeProfileImage() {
        Intent imageIntent = new Intent(Intent.ACTION_PICK);
        imageIntent.setType("image/*");
        startActivityForResult(imageIntent, RC_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SELECT_IMAGE && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                saveImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void saveImageToFirebase() {
        if (imageUri == null) return;

        String fileName = UUID.randomUUID().toString();
        final StorageReference profileImageRef = FirebaseStorage.getInstance()
                .getReference("ProfileImages/"+currentUserId+"/"+fileName);
        profileImageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Profile", "Image Updated");
                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("Profile", "Image: "+uri);
                        userMap.put("image", uri);
                        db.child("Users").child(currentUserId).updateChildren(userMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d("Profile", "Image Uploaded to Firebase");
                                            Toast.makeText(Profile.this, "Image Updated Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("Profile", "Error Uploading Image");
                                            Toast.makeText(Profile.this, "Error Updating Image", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }
}
