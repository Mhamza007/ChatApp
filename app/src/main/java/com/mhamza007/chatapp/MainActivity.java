package com.mhamza007.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SIGNIN";
    private static final int RC_SIGNIN = 121;

    private FirebaseAuth auth;
    private DatabaseReference db;
    private String currentUserId;
    private String currentUserEmail;
    private String currentUserName;
    private HashMap<String, Object > newAccMap;
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference().child("Users");

//        if (networkConnection()){
            if (auth.getCurrentUser() == null){
                createAccount();
            } else {
                startActivity(new Intent(this, UsersActivity.class));
                finish();
            }
//        } else {
//            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
//        }
    }

    private void createAccount() {
        ArrayList<AuthUI.IdpConfig> providers = new ArrayList<>();
        providers.add(new AuthUI.IdpConfig.EmailBuilder().build());
        providers.add(new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.mipmap.ic_launcher_round)
                .setIsSmartLockEnabled(true)
                .build(),
                RC_SIGNIN
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGNIN){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d("Sign In", "RC_SIGN_IN response : "+ response);
            if (resultCode == Activity.RESULT_OK) {
                FirebaseUser user = auth.getCurrentUser();
                currentUserId = user.getUid();
                currentUserEmail = user.getEmail();
                currentUserName = user.getDisplayName();

                newAccMap = new HashMap<>();

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(
                        new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                deviceToken = instanceIdResult.getToken();
                                newAccMap.put("uid", currentUserId);
                                newAccMap.put("Email", currentUserEmail);
                                newAccMap.put("name", currentUserName);
                                newAccMap.put("token", deviceToken);

                                db.child(currentUserId).updateChildren(newAccMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    db.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            startActivity(new Intent(MainActivity.this, UsersActivity.class));
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Log.e(TAG, "Database Error : " + databaseError.toException());
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }

                );
            } else {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean networkConnection(){
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.isConnectedOrConnecting();
    }
}
