package com.mhamza007.chatapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public RelativeLayout userLay;
    public ImageView userImage;
    public TextView userName;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        userLay = itemView.findViewById(R.id.users_list);
        userImage = itemView.findViewById(R.id.user_image);
        userName = itemView.findViewById(R.id.user_name);
    }

}
