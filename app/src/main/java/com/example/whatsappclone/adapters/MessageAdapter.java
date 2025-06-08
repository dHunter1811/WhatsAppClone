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
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<Message> messageList;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    // ViewHolder untuk pesan terkirim
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    // ViewHolder untuk pesan diterima
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text);
            messageTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        // Cek apakah pengirim pesan adalah pengguna saat ini
        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Format timestamp menjadi "HH:mm"
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(message.getTimestamp());
        String time = DateFormat.format("HH:mm", cal).toString();

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.messageText.setText(message.getMessage());
            sentHolder.messageTime.setText(time);
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.messageText.setText(message.getMessage());
            receivedHolder.messageTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
