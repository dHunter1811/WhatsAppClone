package com.example.whatsappclone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.ChatAdapter;
import com.example.whatsappclone.models.ChatItem;
import com.example.whatsappclone.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Variabel baru untuk header
    private CircleImageView userPhotoHeader;
    private TextView userNameHeader;

    public ChatFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Inisialisasi views header
        userPhotoHeader = view.findViewById(R.id.user_photo_header);
        userNameHeader = view.findViewById(R.id.user_name_header);

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList, chatItem -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("chatUid", chatItem.getUid());
            intent.putExtra("chatName", chatItem.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(chatAdapter);

        // Setup FAB
        FloatingActionButton fabNewChat = view.findViewById(R.id.fab_new_chat);
        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ContactsActivity.class);
            startActivity(intent);
        });

        // Panggil metode untuk memuat data
        loadCurrentUserInfo();
        loadChatList();
    }

    // Metode baru untuk memuat info pengguna saat ini
    private void loadCurrentUserInfo() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (getContext() == null) return; // Mencegah crash jika fragment sudah dilepas
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                userNameHeader.setText(user.getName());
                                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                                    Glide.with(getContext()).load(user.getPhotoUrl()).into(userPhotoHeader);
                                }
                            }
                        }
                    });
        }
    }

    private void loadChatList() {
        // ... (metode loadChatList Anda yang sudah ada tetap sama)
        if (currentUser == null) return;
        db.collection("chats")
                .whereArrayContains("participants", currentUser.getUid())
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { return; }
                    if (snapshots == null) return;
                    chatList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants == null) continue;
                        String otherUserId = "";
                        for (String id : participants) {
                            if (!id.equals(currentUser.getUid())) {
                                otherUserId = id;
                                break;
                            }
                        }
                        if (otherUserId.isEmpty()) continue;
                        String finalOtherUserId = otherUserId;
                        db.collection("users").document(otherUserId).get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        User user = userSnapshot.toObject(User.class);
                                        if (user != null) {
                                            ChatItem chatItem = new ChatItem(
                                                    finalOtherUserId,
                                                    user.getName(),
                                                    user.getPhotoUrl(),
                                                    doc.getString("lastMessage"),
                                                    doc.getLong("lastMessageTime")
                                            );
                                            chatList.add(chatItem);
                                            chatAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });
    }
}
