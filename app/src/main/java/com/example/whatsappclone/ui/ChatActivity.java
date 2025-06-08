package com.example.whatsappclone.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.MessageAdapter;
import com.example.whatsappclone.models.MessageItem;
import com.example.whatsappclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView chatName;
    private ImageView profileImage;

    private List<MessageItem> messageList;
    private MessageAdapter adapter;

    private FirebaseFirestore db;
    private ListenerRegistration messageListener;
    private ListenerRegistration userListener;

    private String senderId;
    private String receiverId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatName = findViewById(R.id.chatNameTextView);
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        profileImage = findViewById(R.id.profileImage);

        db = FirebaseFirestore.getInstance();

        // Ambil UID pengirim dan penerima
        receiverId = getIntent().getStringExtra("chatUid");
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatId = generateChatId(senderId, receiverId);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, senderId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listenForMessages();
        listenForUserProfile(receiverId);

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                messageInput.setText("");
            }
        });
    }

    private void sendMessage(String text) {
        MessageItem message = new MessageItem(senderId, text, System.currentTimeMillis());
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message);
    }

    private void listenForMessages() {
        messageListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    messageList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        MessageItem message = doc.toObject(MessageItem.class);
                        messageList.add(message);
                    }

                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void listenForUserProfile(String userId) {
        userListener = db.collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;

                    User user = snapshot.toObject(User.class);
                    if (user != null) {
                        chatName.setText(user.getName());
                        Glide.with(this)
                                .load(user.getPhotoUrl())
                                .placeholder(R.drawable.ic_account_profile)
                                .into(profileImage);
                    }
                });
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
        if (userListener != null) {
            userListener.remove();
        }
    }
}
