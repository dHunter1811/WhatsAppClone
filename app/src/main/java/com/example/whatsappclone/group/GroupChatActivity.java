package com.example.whatsappclone.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.adapters.MessageAdapter;
import com.example.whatsappclone.shared.models.Group;
import com.example.whatsappclone.shared.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main); // Menggunakan kembali layout yang sama

        groupId = getIntent().getStringExtra("GROUP_ID");
        if (groupId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadMessages();

        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageInput = findViewById(R.id.et_message_input);
        sendMessageButton = findViewById(R.id.btn_send_message);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Inisialisasi custom toolbar views
        CircleImageView civProfileImage = findViewById(R.id.civ_profile_image_toolbar);
        TextView tvGroupName = findViewById(R.id.tv_username_toolbar); // Kita gunakan lagi textview ini
        findViewById(R.id.iv_back_arrow).setOnClickListener(v -> finish());

        // Ambil info grup dan tampilkan di toolbar
        db.collection("groups").document(groupId).addSnapshotListener((snapshot, error) -> {
            if (snapshot != null && snapshot.exists()) {
                Group group = snapshot.toObject(Group.class);
                if (group != null) {
                    tvGroupName.setText(group.getGroupName());
                    if (group.getGroupPhotoUrl() != null && !group.getGroupPhotoUrl().isEmpty()) {
                        Glide.with(this).load(group.getGroupPhotoUrl()).into(civProfileImage);
                    }
                }
            }
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, true); // true karena ini chat grup
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        Message message = new Message(messageId, currentUser.getUid(), messageText, timestamp);
        messageInput.setText("");

        DocumentReference groupRef = db.collection("groups").document(groupId);
        // Simpan pesan ke sub-koleksi
        groupRef.collection("messages").document(messageId).set(message);

        // Update info pesan terakhir di dokumen grup utama
        Map<String, Object> lastMessageInfo = new HashMap<>();
        lastMessageInfo.put("lastMessage", messageText);
        lastMessageInfo.put("lastMessageTime", timestamp);
        groupRef.set(lastMessageInfo, SetOptions.merge());
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("groups").document(groupId).collection("messages");
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;
                    if (snapshots != null) {
                        messageList.clear();
                        messageList.addAll(snapshots.toObjects(Message.class));
                        messageAdapter.notifyDataSetChanged();
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }
}
