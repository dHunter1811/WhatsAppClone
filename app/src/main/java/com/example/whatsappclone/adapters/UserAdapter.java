package com.example.whatsappclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.models.User;
import com.example.whatsappclone.ui.ChatActivity;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(context).load(user.getPhotoUrl()).into(holder.userPhoto);
        } else {
            holder.userPhoto.setImageResource(R.mipmap.ic_launcher);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatUid", user.getUid());
            intent.putExtra("chatName", user.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userPhoto;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // PASTIKAN KEDUA ID INI SAMA DENGAN DI item_user.xml
            userPhoto = itemView.findViewById(R.id.civ_user_photo);
            userName = itemView.findViewById(R.id.tv_user_name);
        }
    }
}
