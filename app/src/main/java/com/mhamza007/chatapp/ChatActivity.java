package com.mhamza007.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference dbRef, chatRef, notificationRef;
    ImageButton sendChat;
    EditText chatInput;

    String chatSenderId, chatSenderName, chatReceiverId, chatReveiverName;
    RecyclerView chatList;

    String checker = "";
    Uri fileUri;

    ArrayList<Message> messageList = new ArrayList();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        chatSenderId = auth.getCurrentUser().getUid();
        chatSenderName = auth.getCurrentUser().getDisplayName();
        dbRef = FirebaseDatabase.getInstance().getReference();
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        chatReceiverId = getIntent().getStringExtra("chat_user_id");
        chatReveiverName = getIntent().getStringExtra("chat_user_name");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(chatReveiverName);

        sendChat = findViewById(R.id.send_chat_message);
        chatInput = findViewById(R.id.chat_message_input);
//        sendImage = findViewById(R.id.send_image_btn);

        messageAdapter = new MessageAdapter(messageList);
        chatList = findViewById(R.id.chat_list);
        linearLayoutManager = new LinearLayoutManager(this);
        chatList.setLayoutManager(linearLayoutManager);
        chatList.setAdapter(messageAdapter);


        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

//        sendImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent imagePick = new Intent();
//                imagePick.setAction(Intent.ACTION_GET_CONTENT);
//                imagePick.setType("image/*");
//                checker = "image";
//                startActivityForResult(Intent.createChooser(imagePick, "Select Image"), 15);
//            }
//        });
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 15 && resultCode == Activity.RESULT_OK) {
//            if (data != null && data.getData() != null) {
//                fileUri = data.getData();
//                if (checker == "image"){
//
//                    String chatSRref = "Chat/"+chatSenderId+"/"+chatReceiverId;
//                    String chatRSref = "Chat/"+chatReceiverId+"/"+chatSenderId;
//
//                    chatRef = dbRef.child("Chat").child(chatSenderId).child(chatReceiverId).push();
//
//                    String chatImageKey = chatRef.getKey();
//
//
//                }
//            }
//        }
//    }

    private void sendMessage() {
        final String messageText = chatInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)){
            sendChat.setEnabled(false);
        } else {
            String chatSenderRef = "Chat/"+chatSenderId+"/"+chatReceiverId;
            String chatReceiverRef = "Chat/"+chatReceiverId+"/"+chatSenderId;

            chatRef = dbRef.child("Chat").child(chatSenderId).child(chatReceiverId).push();

            String chatKey = chatRef.getKey();

            HashMap<String, Object> chatText = new HashMap<>();
            chatText.put("message", messageText);
            chatText.put("type", "text");
            chatText.put("from", chatSenderId);
            chatText.put("to", chatReceiverId);
            chatText.put("messageId", chatKey);
            chatText.put("senderName", chatSenderName);
            chatText.put("receiverName", chatReveiverName);

            HashMap<String, Object> chatDetails = new HashMap<>();
            chatDetails.put(chatSenderRef + "/" + chatKey, chatText);
            chatDetails.put(chatReceiverRef + "/" + chatKey, chatText);

            dbRef.updateChildren(chatDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        chatInput.setText("");

                        HashMap <String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", chatSenderId);
                        chatNotificationMap.put("text", messageText);

                        notificationRef.child(chatReceiverId).push().setValue(chatNotificationMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(ChatActivity.this, "Sending Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbRef.child("Chat").child(chatSenderId).child(chatReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();

                        chatList.smoothScrollToPosition(chatList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


}
