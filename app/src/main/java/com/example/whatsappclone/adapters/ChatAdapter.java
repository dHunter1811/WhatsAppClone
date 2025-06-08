package com.example.whatsappclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.models.ChatItem;
import com.example.whatsappclone.ui.ChatActivity;

import java.util.Date;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<ChatItem> chatItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChatItem chatItem);
    }

    public ChatAdapter(Context context, List<ChatItem> chatItems, OnItemClickListener listener) {
        this.context = context;
        this.chatItems = chatItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);
        holder.chatName.setText(item.getName());
        holder.chatLastMessage.setText(item.getLastMessage());

        String time = DateFormat.format("hh:mm a", new Date(item.getLastMessageTime())).toString();
        holder.chatTime.setText(time);

        Glide.with(context)
                .load(item.getPhotoUrl())
                .placeholder(R.drawable.ic_account_profile)
                .circleCrop()
                .into(holder.chatProfileImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView chatProfileImage;
        TextView chatName, chatLastMessage, chatTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatProfileImage = itemView.findViewById(R.id.chatProfileImage);
            chatName = itemView.findViewById(R.id.chatName);
            chatLastMessage = itemView.findViewById(R.id.chatLastMessage);
            chatTime = itemView.findViewById(R.id.chatTime);
        }
    }
}


