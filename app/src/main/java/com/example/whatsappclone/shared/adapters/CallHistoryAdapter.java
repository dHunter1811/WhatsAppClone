package com.example.whatsappclone.shared.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.models.CallHistory;
import com.example.whatsappclone.shared.models.User;
import com.example.whatsappclone.shared.utils.TimeAgo;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder> {

    private final Context context;
    private final List<CallHistory> callHistoryList;

    public CallHistoryAdapter(Context context, List<CallHistory> callHistoryList) {
        this.context = context;
        this.callHistoryList = callHistoryList;
    }

    @NonNull
    @Override
    public CallHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_call_history, parent, false);
        return new CallHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallHistoryViewHolder holder, int position) {
        CallHistory history = callHistoryList.get(position);

        // ... (kode untuk waktu, jenis panggilan, dan arah panggilan tetap sama) ...
        holder.callTime.setText(TimeAgo.getTimeAgo(history.getTimestamp()));
        if ("video".equals(history.getCallType())) {
            holder.callBackType.setImageResource(R.drawable.ic_action_videocam);
        } else {
            holder.callBackType.setImageResource(R.drawable.ic_action_call);
        }
        if ("outgoing".equals(history.getCallDirection())) {
            holder.callDirection.setImageResource(R.drawable.ic_action_call_made);
            holder.callDirection.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.callDirection.setImageResource(R.drawable.ic_action_call_received);
            holder.callDirection.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        }

        FirebaseFirestore.getInstance().collection("users").document(history.getOtherUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            holder.userName.setText(user.getName());

                            // --- PERBAIKAN DI SINI ---
                            // Menggunakan nama metode yang benar: getProfileImageUrl()
                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(context).load(user.getProfileImageUrl()).into(holder.userPhoto);
                            } else {
                                holder.userPhoto.setImageResource(R.mipmap.ic_launcher);
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return callHistoryList.size();
    }

    static class CallHistoryViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userPhoto;
        ImageView callDirection, callBackType;
        TextView userName, callTime;
        public CallHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.civ_user_photo);
            callDirection = itemView.findViewById(R.id.iv_call_direction);
            callBackType = itemView.findViewById(R.id.iv_call_back_type);
            userName = itemView.findViewById(R.id.tv_user_name);
            callTime = itemView.findViewById(R.id.tv_call_time);
        }
    }
}
