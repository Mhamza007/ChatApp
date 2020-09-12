package com.mhamza007.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Message> userMessageList;
    FirebaseAuth auth;
    DatabaseReference dbRef;

    public MessageAdapter(List<Message> userMessageList) {
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_messages_layout, parent, false);
        auth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String chatSenderId = auth.getCurrentUser().getUid();
        Message message = userMessageList.get(position);

        String fromUserId = message.getFrom();
        String fromMessageType = message.getType();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("image")){
                        String receiverImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image)
                                .into(holder.receiverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        holder.receiverMessageText.setVisibility(View.GONE);
//        holder.receiverProfileImage.setVisibility(View.GONE);
//        holder.senderMessageText.setVisibility(View.GONE);
//        holder.messageSendImage.setVisibility(View.GONE);
//        holder.messageReceiveImage.setVisibility(View.GONE);

        if (fromMessageType.equals("text")){

            holder.receiverMessageText.setVisibility(View.GONE);
            holder.receiverProfileImage.setVisibility(View.GONE);

            if (fromUserId.equals(chatSenderId)){
                holder.senderMessageText.setBackgroundResource(R.drawable.chat_sender_layout);
                holder.senderMessageText.setText(message.getMessage());
            } else {
                holder.senderMessageText.setVisibility(View.GONE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.chat_receiver_layout);
                holder.receiverMessageText.setText(message.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        CircleImageView receiverProfileImage;
//        ImageView messageSendImage, messageReceiveImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_chat_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_chat_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
//            messageSendImage = itemView.findViewById(R.id.message_send_image_view);
//            messageReceiveImage = itemView.findViewById(R.id.message_receive_image_view);
        }

    }
}
