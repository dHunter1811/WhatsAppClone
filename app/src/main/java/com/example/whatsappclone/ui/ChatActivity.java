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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.Random;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    // UI Views
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton;

    private ImageButton btnPantun; // <-- Variabel baru untuk tombol pantun


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

        // --- PERBAIKAN PENTING: Pemeriksaan Keamanan ---
        // Jika karena suatu alasan ID tidak terkirim, tutup activity ini
        // agar tidak terjadi crash atau menampilkan halaman kosong.
        if (receiverUid == null || receiverUid.isEmpty()) {
            Toast.makeText(this, "Gagal membuka chat. Pengguna tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi semua komponen
        initViews();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();

        // Buat ID chat dan muat data
        chatId = generateChatId(currentUser.getUid(), receiverUid);
        loadReceiverInfo();
        loadMessages();
    }

    private void initViews() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Custom Toolbar Views
        civProfileImage = findViewById(R.id.civ_profile_image_toolbar);
        tvUsername = findViewById(R.id.tv_username_toolbar);
        tvOnlineStatus = findViewById(R.id.tv_online_status_toolbar);
        ivBackArrow = findViewById(R.id.iv_back_arrow);
        ivVideoCall = findViewById(R.id.iv_video_call);
        ivCall = findViewById(R.id.iv_call);
        ivMore = findViewById(R.id.iv_more);

        // Main UI
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageInput = findViewById(R.id.et_message_input);
        sendMessageButton = findViewById(R.id.btn_send_message);
        btnPantun = findViewById(R.id.btn_pantun); // <-- Inisialisasi tombol baru

    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        // Mengirim 'false' karena ini bukan chat grup
        messageAdapter = new MessageAdapter(this, messageList, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Pesan baru akan mendorong list ke atas
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        ivBackArrow.setOnClickListener(v -> finish());
        sendMessageButton.setOnClickListener(v -> sendMessage());
        btnPantun.setOnClickListener(v -> generateRandomPantun());
        ivVideoCall.setOnClickListener(v -> initiateCall("video"));
        ivCall.setOnClickListener(v -> initiateCall("voice"));
        ivMore.setOnClickListener(v -> Toast.makeText(this, "Opsi lain diklik", Toast.LENGTH_SHORT).show());
    }

    private void loadReceiverInfo() {
        DocumentReference receiverRef = db.collection("users").document(receiverUid);
        receiverRef.addSnapshotListener(this, (snapshot, error) -> {
            if (error != null) { return; }

            if (snapshot != null && snapshot.exists()) {
                String name = snapshot.getString("name");
                String photoUrl = snapshot.getString("profileImageUrl");

                tvUsername.setText(name);

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).into(civProfileImage);
                } else {
                    civProfileImage.setImageResource(R.mipmap.ic_launcher); // Gambar default
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

    // --- DAFTAR PANTUN KITA ---
    private final List<String> pantunList = Arrays.asList(
            // Pantun romantis
            "Waktu daftar hari terakhir,\nWaktu terasa banyak terbuang.\nKamu nggak perlu khawatir,\nCintaku hanya untukmu seorang.",
            "Api kecil dari tungku,\nApinya kecil habis kayu.\nSudah lama kutunggu-tunggu,\nKapan kamu bilang i love you.",
            "Ayam goreng setengah mateng,\nBelinya di depan tugu.\nAbang sayang, abangku ganteng,\nNeng di sini setia menunggu.",
            "Jalanan lagi lancar,\nItu adalah sebuah berkah.\nAku bukan nyari pacar,\nTapi, nyari yang mau diajak nikah.",
            "Minum sekoteng hangat rasanya,\nMinum segelas ada yang minta.\nLaki-laki ganteng siapa yang punya?\nBolehkah aku jatuh cinta?",

            // Pantun lucu absurd
            "Bocah kecil suka bermain,\nMainannya boneka musang.\nJangan suka bergantung pada orang lain,\nApalagi bergantung sambil makan pisang.",
            "Keluarga besar banyak anak,\nSalah satu anak namanya Wawa.\nBersikaplah mirip kuntilanak,\nSenang duka selalu tertawa.",
            "Pagi hari minum jamu,\nBiar kuat, banyak tenaga.\nJanganlah menuntut ilmu,\nKarena ilmu tidak bersalah.",
            "Pergi ke Solo menonton wayang,\nTidak lupa membawa bekal.\nBanyak uang aku disayang,\nTak ada uang, malah ditinggal.",
            "Jalan-jalan ke pinggir empang,\nNemu katak di pinggir empang.\nHati siapa tak bimbang,\nKamu botak minta dikepang.",

            // Pantun pendidikan / bijak
            "Pergi ke pantai melihat laut biru,\nPemandangannya indah membuat hati penuh warna.\nEngkau masih kecil tuntutlah ilmu,\nSebagai bekal kelak saat dewasa.",
            "Bungkuslah ikan dengan kertas,\nLalu ditaruh di atas talas.\nJika pendidikan berkualitas,\nPasti jadi bangsa berkelas.",
            "Jika perahu sudah melaju,\nMari kita banyak berdoa.\nJika ingin negeri maju,\nPendidikan harus utama.",
            "Tubuh letih usah dipaksakan,\nNanti sakit sekujur tubuh.\nPendidikan jangan disia-siakan,\nItulah jalan menuju maju.",
            "Luas padang rerumputan,\nAda domba banyak delapan.\nPendidikan ibarat jembatan,\nJembatan menuju masa depan."
    );

    private final Random random = new Random();

    private void generateRandomPantun() {
        if (pantunList.isEmpty()) return;

        // Pilih pantun acak dari daftar
        int randomIndex = random.nextInt(pantunList.size());
        String pantun = pantunList.get(randomIndex);

        // Masukkan pantun ke dalam kolom input
        messageInput.setText(pantun);
        // Pindahkan kursor ke akhir teks
        messageInput.setSelection(pantun.length());
    }
}
