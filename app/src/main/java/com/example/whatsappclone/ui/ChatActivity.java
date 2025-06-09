package com.example.whatsappclone.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.MessageAdapter;
import com.example.whatsappclone.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    // UI Views
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;

    // Custom Toolbar Views
    private CircleImageView civProfileImage;
    private TextView tvUsername, tvOnlineStatus;
    private ImageView ivBackArrow, ivVideoCall, ivCall, ivMore;

    // Firebase & Data
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String receiverUid;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ambil data dari Intent
        receiverUid = getIntent().getStringExtra("chatUid");

        // Inisialisasi Firebase & Views
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Inisialisasi custom toolbar views
        civProfileImage = findViewById(R.id.civ_profile_image_toolbar);
        tvUsername = findViewById(R.id.tv_username_toolbar);
        tvOnlineStatus = findViewById(R.id.tv_online_status_toolbar);
        ivBackArrow = findViewById(R.id.iv_back_arrow);
        ivVideoCall = findViewById(R.id.iv_video_call);
        ivCall = findViewById(R.id.iv_call);
        ivMore = findViewById(R.id.iv_more);

        // Inisialisasi UI utama
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageInput = findViewById(R.id.et_message_input);
        sendMessageButton = findViewById(R.id.btn_send_message);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        if (currentUser != null && receiverUid != null) {
            chatId = generateChatId(currentUser.getUid(), receiverUid);
            loadReceiverInfo();
            loadMessages();
        }

        // Setup listener untuk semua tombol
        setupClickListeners();
    }

    private void setupClickListeners(){
        ivBackArrow.setOnClickListener(v -> finish());
        sendMessageButton.setOnClickListener(v -> sendMessage());
        ivVideoCall.setOnClickListener(v -> Toast.makeText(this, "Video Call diklik", Toast.LENGTH_SHORT).show());
        ivCall.setOnClickListener(v -> Toast.makeText(this, "Call diklik", Toast.LENGTH_SHORT).show());
        ivMore.setOnClickListener(v -> Toast.makeText(this, "More options diklik", Toast.LENGTH_SHORT).show());
    }

    private void loadReceiverInfo() {
        // ... (kode loadReceiverInfo tetap sama seperti sebelumnya)
        DocumentReference receiverRef = db.collection("users").document(receiverUid);
        receiverRef.addSnapshotListener(this, (snapshot, error) -> {
            if (error != null) { Log.e("ChatActivity", "Gagal mendengarkan status: ", error); return; }
            if (snapshot != null && snapshot.exists()) {
                String name = snapshot.getString("name");
                tvUsername.setText(name);
                String photoUrl = snapshot.getString("profileImageUrl");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).into(civProfileImage);
                }
                if (snapshot.contains("onlineStatus")) {
                    String status = snapshot.getString("onlineStatus");
                    tvOnlineStatus.setText(status);
                    tvOnlineStatus.setVisibility(View.VISIBLE);
                } else {
                    tvOnlineStatus.setVisibility(View.GONE);
                }
            }
        });
    }

    private String generateChatId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) return uid1 + "_" + uid2;
        else return uid2 + "_" + uid1;
    }

    private void sendMessage() {
        // ... (kode sendMessage tetap sama seperti sebelumnya)
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        Message message = new Message(messageId, currentUser.getUid(), messageText, timestamp);
        messageInput.setText("");
        db.collection("chats").document(chatId).collection("messages")
                .document(messageId).set(message);
    }

    private void loadMessages() {
        // ... (kode loadMessages tetap sama seperti sebelumnya)
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");
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

    // HAPUS metode onCreateOptionsMenu dan onOptionsItemSelected dari sini
}
