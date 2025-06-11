package com.example.whatsappclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.models.Group;
import com.example.whatsappclone.utils.TimeAgo;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final Context context;
    private final List<Group> groupList;
    private final OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupAdapter(Context context, List<Group> groupList, OnGroupClickListener listener) {
        this.context = context;
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.bind(group, listener);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupPhoto;
        TextView groupName, lastMessage, lastMessageTime;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupPhoto = itemView.findViewById(R.id.iv_group_photo);
            groupName = itemView.findViewById(R.id.tv_group_name);
            lastMessage = itemView.findViewById(R.id.tv_last_message);
            lastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
        }

        void bind(final Group group, final OnGroupClickListener listener) {
            groupName.setText(group.getGroupName());
            lastMessage.setText(group.getLastMessage());

            if (group.getLastMessageTime() > 0) {
                lastMessageTime.setText(TimeAgo.getTimeAgo(group.getLastMessageTime()));
            } else {
                lastMessageTime.setText("");
            }

            if (group.getGroupPhotoUrl() != null && !group.getGroupPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(group.getGroupPhotoUrl()).into(groupPhoto);
            } else {
                groupPhoto.setImageResource(R.drawable.ic_camera); // Gambar default grup
            }

            itemView.setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}
