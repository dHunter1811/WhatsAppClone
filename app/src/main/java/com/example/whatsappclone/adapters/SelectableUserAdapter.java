package com.example.whatsappclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.models.User;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class SelectableUserAdapter extends RecyclerView.Adapter<SelectableUserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;
    private final List<User> selectedUsers = new ArrayList<>();

    public SelectableUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_with_checkbox, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userPhoto;
        TextView userName;
        CheckBox userCheckBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.civ_user_photo);
            userName = itemView.findViewById(R.id.tv_user_name);
            userCheckBox = itemView.findViewById(R.id.cb_select_user);
        }

        void bind(final User user) {
            userName.setText(user.getName());
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(context).load(user.getPhotoUrl()).into(userPhoto);
            } else {
                userPhoto.setImageResource(R.mipmap.ic_launcher);
            }

            // Atur status checkbox berdasarkan daftar yang dipilih
            userCheckBox.setChecked(selectedUsers.contains(user));

            itemView.setOnClickListener(v -> {
                userCheckBox.setChecked(!userCheckBox.isChecked());
                if (userCheckBox.isChecked()) {
                    if (!selectedUsers.contains(user)) {
                        selectedUsers.add(user);
                    }
                } else {
                    selectedUsers.remove(user);
                }
            });
        }
    }
}
