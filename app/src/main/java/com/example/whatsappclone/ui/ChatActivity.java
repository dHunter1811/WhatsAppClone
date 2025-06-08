package com.example.whatsappclone.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.MessageAdapter;
import com.example.whatsappclone.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    // UI Views
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;
    private Toolbar toolbar;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Data
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String receiverUid;
    private String receiverName;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ambil data dari Intent
        receiverName = getIntent().getStringExtra("chatName");
        receiverUid = getIntent().getStringExtra("chatUid");

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Inisialisasi Views
        toolbar = findViewById(R.id.toolbar_chat);
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageInput = findViewById(R.id.et_message_input);
        sendMessageButton = findViewById(R.id.btn_send_message);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(receiverName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Tampilkan tombol kembali
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Aksi tombol kembali

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Pesan baru muncul dari bawah
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Generate ID unik untuk ruang obrolan
        if (currentUser != null && receiverUid != null) {
            chatId = generateChatId(currentUser.getUid(), receiverUid);
            loadMessages();
        }

        // Setup tombol kirim
        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private String generateChatId(String uid1, String uid2) {
        // Urutkan UID secara alfabetis untuk memastikan ID chat konsisten
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
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

        // Buat objek pesan baru
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        Message message = new Message(messageId, currentUser.getUid(), messageText, timestamp);

        // Kosongkan input field
        messageInput.setText("");

        // Simpan pesan ke Firestore
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener(aVoid -> {
                    // Pesan berhasil dikirim
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Gagal mengirim pesan.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");

        // "Dengarkan" perubahan pada koleksi pesan secara real-time
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Gagal memuat pesan.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        messageList.clear();
                        for (Message message : snapshots.toObjects(Message.class)) {
                            messageList.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        // Scroll ke pesan paling bawah
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }
}
