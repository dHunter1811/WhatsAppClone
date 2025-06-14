package com.example.whatsappclone.main.fragments;

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
import com.example.whatsappclone.shared.adapters.ChatAdapter;
import com.example.whatsappclone.shared.models.ChatItem;
import com.example.whatsappclone.shared.models.User;
import com.example.whatsappclone.chat.ChatActivity;
import com.example.whatsappclone.contacts.ContactsActivity;
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

    // TAG untuk mempermudah pencarian di Logcat
    private static final String TAG = "ChatFragmentDebug";

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter; // <-- Nama variabelnya adalah chatAdapter

    private List<ChatItem> chatList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CircleImageView userPhotoHeader;
    private TextView userNameHeader;

    public ChatFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews(view);
        setupRecyclerView();
        setupFab(view);

        loadCurrentUserInfo();
        loadChatList();
    }

    private void initViews(View view) {
        userPhotoHeader = view.findViewById(R.id.user_photo_header);
        userNameHeader = view.findViewById(R.id.user_name_header);
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
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
    }

    private void setupFab(View view) {
        FloatingActionButton fabNewChat = view.findViewById(R.id.fab_new_chat);
        fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ContactsActivity.class));
        });
    }

    private void loadCurrentUserInfo() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (getContext() == null) return;
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                userNameHeader.setText(user.getName());
                                Glide.with(getContext()).load(user.getProfileImageUrl()).into(userPhotoHeader);
                            }
                        }
                    });
        }
    }

    private void loadChatList() {
        if (currentUser == null) return;
        Log.d(TAG, "Memulai loadChatList...");

        db.collection("chats")
                .whereArrayContains("participants", currentUser.getUid())
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore listener error", error);
                        return;
                    }
                    if (snapshots == null) return;

                    Log.d(TAG, "Ditemukan " + snapshots.size() + " percakapan.");
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

                        Log.d(TAG, "Memproses chat dengan: " + otherUserId);

                        String finalOtherUserId = otherUserId;
                        db.collection("users").document(otherUserId).get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        Log.d(TAG, "--- DATA MENTAH DARI FIRESTORE untuk " + finalOtherUserId + " ---");
                                        Log.d(TAG, "Data: " + userSnapshot.getData());

                                        User user = userSnapshot.toObject(User.class);
                                        if (user != null) {
                                            Log.d(TAG, "--- DATA SETELAH DIUBAH KE OBJEK USER ---");
                                            Log.d(TAG, "User.getName(): " + user.getName());
                                            Log.d(TAG, "User.getPhotoUrl(): " + user.getProfileImageUrl()); // INI PALING PENTING

                                            ChatItem chatItem = new ChatItem(
                                                    finalOtherUserId,
                                                    user.getName(),
                                                    user.getProfileImageUrl(),
                                                    doc.getString("lastMessage"),
                                                    doc.getLong("lastMessageTime")
                                            );
                                            chatList.add(chatItem);
                                            // --- PERBAIKAN DI SINI ---
                                            // Pastikan menggunakan nama variabel yang benar
                                            chatAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.e(TAG, "Objek User null setelah .toObject()");
                                        }
                                    } else {
                                        Log.w(TAG, "Dokumen user tidak ditemukan untuk ID: " + finalOtherUserId);
                                    }
                                });
                    }
                });
    }
}
