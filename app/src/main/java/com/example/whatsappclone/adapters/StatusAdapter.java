package com.example.whatsappclone.adapters;

// File: adapters/StatusAdapter.java
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatsappclone.R; // Ganti dengan package Anda
import com.example.whatsappclone.models.Status; // Ganti dengan package Anda
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
// Gunakan library Glide atau Picasso untuk memuat gambar dari URL
// implementation 'com.github.bumptech.glide:glide:4.12.0'
import com.bumptech.glide.Glide;
import com.example.whatsappclone.ui.StatusViewerActivity;
import com.example.whatsappclone.utils.TimeAgo;


public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private Context context;
    private List<Status> statusList;

    public StatusAdapter(Context context, List<Status> statusList) {
        this.context = context;
        this.statusList = statusList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        Status currentStatus = statusList.get(position);

        Log.d("StatusAdapter", "Data akan ditampilkan: Nama=" + currentStatus.getUserName() + ", URL Foto=" + currentStatus.getProfileImageUrl());

        holder.userName.setText(currentStatus.getUserName());

        String timeAgo = TimeAgo.getTimeAgo(currentStatus.getLastUpdated());
        holder.timestamp.setText(timeAgo);

        // Muat gambar profil menggunakan Glide/Picasso
        Glide.with(context)
                .load(currentStatus.getProfileImageUrl())
                .placeholder(R.mipmap.ic_launcher) // Gambar default
                .into(holder.profilePic);

        // Tambahkan OnClickListener untuk membuka status
        holder.itemView.setOnClickListener(v -> {
            // Logika untuk membuka status viewer
            Intent intent = new Intent(context, StatusViewerActivity.class);
            // Kirim seluruh objek Status ke activity berikutnya
            intent.putExtra("status_object", currentStatus);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView userName;
        TextView timestamp;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.iv_profile_pic_status);
            userName = itemView.findViewById(R.id.tv_user_name_status);
            timestamp = itemView.findViewById(R.id.tv_timestamp_status);
        }
    }
}
