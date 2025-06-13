package com.example.whatsappclone.shared.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.models.Message;
import com.example.whatsappclone.shared.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<Message> messageList;
    private final boolean isGroupChat;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_RECEIVED_GROUP = 3;

    public MessageAdapter(Context context, List<Message> messageList, boolean isGroupChat) {
        this.context = context;
        this.messageList = messageList;
        this.isGroupChat = isGroupChat;
    }

    // --- ViewHolder untuk setiap tipe pesan ---

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        ImageView readStatusIcon;
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
            readStatusIcon = itemView.findViewById(R.id.iv_read_status);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    static class GroupReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, senderName;
        public GroupReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
            senderName = itemView.findViewById(R.id.tv_sender_name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return VIEW_TYPE_RECEIVED;
        boolean isSender = message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (isSender) {
            return VIEW_TYPE_SENT;
        } else {
            return isGroupChat ? VIEW_TYPE_RECEIVED_GROUP : VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else { // VIEW_TYPE_RECEIVED_GROUP
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received_group, parent, false);
            return new GroupReceivedMessageViewHolder(view);
        }
    }

    // --- PERBAIKAN UTAMA ADA DI METODE INI ---
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = DateFormat.format("HH:mm", message.getTimestamp()).toString();

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT: { // Menggunakan kurung kurawal untuk mendefinisikan scope
                SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
                sentHolder.messageText.setText(message.getMessage());
                sentHolder.messageTime.setText(time);

                if (message.isRead()) {
                    sentHolder.readStatusIcon.setVisibility(View.VISIBLE);
                    // Tambahkan null check untuk context demi keamanan
                    if (context != null) {
                        sentHolder.readStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.blue_tick_color));
                    }
                } else {
                    sentHolder.readStatusIcon.setVisibility(View.GONE);
                }
                break;
            }
            case VIEW_TYPE_RECEIVED: {
                ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
                receivedHolder.messageText.setText(message.getMessage());
                receivedHolder.messageTime.setText(time);
                break;
            }
            case VIEW_TYPE_RECEIVED_GROUP: {
                GroupReceivedMessageViewHolder groupHolder = (GroupReceivedMessageViewHolder) holder;
                groupHolder.messageText.setText(message.getMessage());
                groupHolder.messageTime.setText(time);

                FirebaseFirestore.getInstance().collection("users").document(message.getSenderId()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                User sender = documentSnapshot.toObject(User.class);
                                if (sender != null) {
                                    groupHolder.senderName.setText(sender.getName());
                                }
                            }
                        });
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
