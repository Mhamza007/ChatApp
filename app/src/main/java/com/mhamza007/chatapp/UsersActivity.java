package com.mhamza007.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    RecyclerView usersList;
    FirebaseAuth auth;
    String currentUserId;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().getRef().child("Users");

        usersList = findViewById(R.id.users_list);
        usersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(userRef, User.class)
                .build();

        FirebaseRecyclerAdapter<User, UserViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, final int i, @NonNull User user) {
                        String userIds = getRef(i).getKey();
                        userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){
                                        String userImage = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher).into(userViewHolder.userImage);
                                    }
                                    final String userName = dataSnapshot.child("name").getValue().toString();
                                    userViewHolder.userName.setText(userName);

                                    userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //Generate and send the key and send to new Activity
                                            String userIds = getRef(i).getKey();
                                            Intent chatIntent = new Intent(UsersActivity.this, ChatActivity.class);
                                            chatIntent.putExtra("chat_user_id", userIds);
                                            chatIntent.putExtra("chat_user_name", userName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                } else {
                                    Log.e("User", "Data snapshot doesn't exists");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Users", "Database Error: " + databaseError.toException());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.user_item, parent, false);
                        return new UserViewHolder(view);
                    }
                };
        usersList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out: {
                AuthUI.getInstance().signOut(this).addOnCompleteListener ( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(UsersActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
            case R.id.profile : {
                startActivity(new Intent(UsersActivity.this, Profile.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
