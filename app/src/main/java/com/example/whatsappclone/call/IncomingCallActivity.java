package com.example.whatsappclone.call;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class IncomingCallActivity extends AppCompatActivity {

    private String callId, channelName, callerName, callerPhotoUrl, callType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_incoming);

        // Ambil data dari Intent
        callId = getIntent().getStringExtra("callId");
        channelName = getIntent().getStringExtra("channelName");
        callerName = getIntent().getStringExtra("callerName");
        callerPhotoUrl = getIntent().getStringExtra("callerPhotoUrl");
        callType = getIntent().getStringExtra("callType");

        // Inisialisasi Views
        CircleImageView ivCallerPhoto = findViewById(R.id.iv_caller_photo);
        TextView tvCallerName = findViewById(R.id.tv_caller_name);
        TextView tvCallType = findViewById(R.id.tv_call_type);
        ImageButton btnDecline = findViewById(R.id.btn_decline);
        ImageButton btnAccept = findViewById(R.id.btn_accept);

        // Set data ke Views
        tvCallerName.setText(callerName);
        if (callType.equals("video")) {
            tvCallType.setText("Panggilan Video WhatsApp");
        } else {
            tvCallType.setText("Panggilan Suara WhatsApp");
        }

        if (callerPhotoUrl != null && !callerPhotoUrl.isEmpty()) {
            Glide.with(this).load(callerPhotoUrl).into(ivCallerPhoto);
        }

        // Setup listener tombol
        btnDecline.setOnClickListener(v -> declineCall());
        btnAccept.setOnClickListener(v -> acceptCall());
    }

    private void acceptCall() {
        // Perbarui status panggilan di Firestore menjadi "accepted"
        FirebaseFirestore.getInstance().collection("calls").document(callId)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    // Mulai CallActivity untuk bergabung ke channel
                    Intent intent = new Intent(IncomingCallActivity.this, CallActivity.class);
                    intent.putExtra("channelName", channelName);
                    intent.putExtra("callId", callId);
                    startActivity(intent);
                    finish(); // Tutup layar ini
                });
    }

    private void declineCall() {
        // Perbarui status panggilan di Firestore menjadi "declined"
        FirebaseFirestore.getInstance().collection("calls").document(callId)
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> finish()); // Tutup layar ini
    }
}
