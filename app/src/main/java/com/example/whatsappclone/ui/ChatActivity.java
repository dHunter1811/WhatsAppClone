package com.example.whatsappclone.ui;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.MessageAdapter;
import com.example.whatsappclone.models.Call;
import com.example.whatsappclone.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.firebase.firestore.SetOptions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    // ... (variabel UI, Firebase, dan Data lainnya tetap sama) ...
    private CircleImageView civProfileImage;
    private TextView tvUsername, tvOnlineStatus;
    private ImageView ivBackArrow, ivVideoCall, ivCall, ivMore;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;
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

        // ... (kode inisialisasi yang sudah ada di onCreate tetap sama) ...
        receiverUid = getIntent().getStringExtra("chatUid");
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        civProfileImage = findViewById(R.id.civ_profile_image_toolbar);
        tvUsername = findViewById(R.id.tv_username_toolbar);
        tvOnlineStatus = findViewById(R.id.tv_online_status_toolbar);
        ivBackArrow = findViewById(R.id.iv_back_arrow);
        ivVideoCall = findViewById(R.id.iv_video_call);
        ivCall = findViewById(R.id.iv_call);
        ivMore = findViewById(R.id.iv_more);
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageInput = findViewById(R.id.et_message_input);
        sendMessageButton = findViewById(R.id.btn_send_message);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        if (currentUser != null && receiverUid != null) {
            chatId = generateChatId(currentUser.getUid(), receiverUid);
            loadReceiverInfo();
            loadMessages();
        }
        setupClickListeners();
    }

    private void setupClickListeners() {
        ivBackArrow.setOnClickListener(v -> finish());
        sendMessageButton.setOnClickListener(v -> sendMessage());

        // --- PERUBAHAN UTAMA DI SINI ---
        ivVideoCall.setOnClickListener(v -> initiateCall("video"));
        ivCall.setOnClickListener(v -> initiateCall("voice"));
        ivMore.setOnClickListener(v -> Toast.makeText(this, "More options diklik", Toast.LENGTH_SHORT).show());
    }

    private void initiateCall(String callType) {
        if (currentUser == null || receiverUid == null) {
            Toast.makeText(this, "Tidak bisa memulai panggilan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil info profil kita sendiri untuk dikirim dalam "undangan"
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String callerName = documentSnapshot.getString("name");
                        String callerPhotoUrl = documentSnapshot.getString("profileImageUrl");

                        // Buat ID unik untuk panggilan dan channel
                        String callId = UUID.randomUUID().toString();

                        // Buat objek Call baru
                        Call newCall = new Call(
                                callId,
                                currentUser.getUid(),
                                callerName,
                                callerPhotoUrl,
                                receiverUid,
                                callType,
                                "dialing",
                                System.currentTimeMillis()
                        );

                        // Simpan "undangan" panggilan ke Firestore
                        db.collection("calls").document(callId).set(newCall)
                                .addOnSuccessListener(aVoid -> {
                                    // Jika undangan berhasil dibuat, mulai CallActivity
                                    Intent intent = new Intent(ChatActivity.this, CallActivity.class);
                                    // Kirim data penting ke CallActivity
                                    intent.putExtra("channelName", callId); // Gunakan callId sebagai nama channel
                                    intent.putExtra("callId", callId);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Gagal memulai panggilan.", Toast.LENGTH_SHORT).show());
                    }
                });
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
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return; // Jangan kirim pesan kosong
        }

        if (currentUser == null || chatId == null) {
            Toast.makeText(this, "Gagal mengirim pesan, coba lagi.", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        Message message = new Message(messageId, currentUser.getUid(), messageText, timestamp);
        messageInput.setText("");

        // --- LOGIKA BARU UNTUK UPDATE DOKUMEN CHAT ---
        DocumentReference chatRef = db.collection("chats").document(chatId);

        // Siapkan data untuk update di dokumen chat induk
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("lastMessage", messageText);
        chatData.put("lastMessageTime", timestamp);
        // Simpan juga ID kedua partisipan untuk mempermudah query nanti
        chatData.put("participants", Arrays.asList(currentUser.getUid(), receiverUid));

        // Gunakan .set() dengan SetOptions.merge() untuk membuat dokumen jika belum ada,
        // atau memperbaruinya jika sudah ada.
        chatRef.set(chatData, SetOptions.merge());

        // Simpan pesan ke sub-koleksi 'messages'
        chatRef.collection("messages").document(messageId).set(message);
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");

        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { return; }

                    if (snapshots != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                // --- LOGIKA BARU UNTUK MENANDAI SEBAGAI DIBACA ---
                                if (!message.getSenderId().equals(currentUser.getUid()) && !message.isRead()) {
                                    // Jika pesan dari lawan bicara dan belum dibaca, update di Firestore
                                    doc.getReference().update("read", true);
                                }
                                messageList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    // HAPUS metode onCreateOptionsMenu dan onOptionsItemSelected dari sini
}
