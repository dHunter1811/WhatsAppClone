package com.example.whatsappclone.shared.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.models.ChatItem;
import com.example.whatsappclone.shared.utils.TimeAgo;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatItem> chatList;
    private final OnChatItemClickListener listener;

    public interface OnChatItemClickListener {
        void onChatItemClick(ChatItem chatItem);
    }

    public ChatAdapter(Context context, List<ChatItem> chatList, OnChatItemClickListener listener) {
        this.context = context;
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem currentItem = chatList.get(position);
        holder.bind(currentItem, listener);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView userName, lastMessage, lastMessageTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.iv_profile_pic_chat_item);
            userName = itemView.findViewById(R.id.tv_user_name_chat_item);
            lastMessage = itemView.findViewById(R.id.tv_last_message);
            lastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
        }

        // --- PERBAIKAN DI SINI ---
        public void bind(final ChatItem chatItem, final OnChatItemClickListener listener) {
            userName.setText(chatItem.getName());
            lastMessage.setText(chatItem.getLastMessage());
            if (chatItem.getLastMessageTime() > 0) {
                lastMessageTime.setText(TimeAgo.getTimeAgo(chatItem.getLastMessageTime()));
            } else {
                lastMessageTime.setText("");
            }
            if (chatItem.getPhotoUrl() != null && !chatItem.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(chatItem.getPhotoUrl()) // <-- PASTIKAN INI BENAR
                        .placeholder(R.mipmap.ic_launcher)
                        .into(profilePic);
            } else {
                profilePic.setImageResource(R.mipmap.ic_launcher);
            }

            // Saat item diklik, panggil listener yang ada di ChatFragment
            itemView.setOnClickListener(v -> listener.onChatItemClick(chatItem));
        }
    }
}
