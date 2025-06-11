package com.example.whatsappclone.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.models.Message;
import com.example.whatsappclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    // --- PERBAIKAN UTAMA ADA DI SINI ---

    // ViewHolder untuk pesan terkirim
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView); // Memanggil konstruktor superclass dengan View
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    // ViewHolder untuk pesan diterima (1-on-1)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView); // Memanggil konstruktor superclass dengan View
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    // ViewHolder untuk pesan diterima di grup
    static class GroupReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, senderName;
        public GroupReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView); // Memanggil konstruktor superclass dengan View
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
            senderName = itemView.findViewById(R.id.tv_sender_name);
        }
    }

    // --- SISA KODE TETAP SAMA ---

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        // Cek null untuk getCurrentUser() untuk keamanan
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = DateFormat.format("HH:mm", message.getTimestamp()).toString();

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT:
                ((SentMessageViewHolder) holder).messageText.setText(message.getMessage());
                ((SentMessageViewHolder) holder).messageTime.setText(time);
                break;
            case VIEW_TYPE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).messageText.setText(message.getMessage());
                ((ReceivedMessageViewHolder) holder).messageTime.setText(time);
                break;
            case VIEW_TYPE_RECEIVED_GROUP:
                GroupReceivedMessageViewHolder groupHolder = (GroupReceivedMessageViewHolder) holder;
                groupHolder.messageText.setText(message.getMessage());
                groupHolder.messageTime.setText(time);
                // Ambil nama pengirim dari Firestore
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

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
