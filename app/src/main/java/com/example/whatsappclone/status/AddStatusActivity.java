package com.example.whatsappclone.status;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.whatsappclone.R; // Ganti dengan package Anda
import com.example.whatsappclone.shared.models.Status;
import com.example.whatsappclone.shared.models.Story;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddStatusActivity extends AppCompatActivity {

    private ImageView previewImageView;
    private Button selectImageButton, postStatusButton;
    private ProgressBar progressBar;
    private Uri imageUri;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    previewImageView.setImageURI(imageUri);
                    postStatusButton.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_add);

        previewImageView = findViewById(R.id.iv_preview_status);
        selectImageButton = findViewById(R.id.btn_select_image);
        postStatusButton = findViewById(R.id.btn_post_status);
        progressBar = findViewById(R.id.progress_bar_upload);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        selectImageButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        postStatusButton.setOnClickListener(v -> uploadStatus());
    }

    private void uploadStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (imageUri != null && currentUser != null) {
            // 1. Tampilkan ProgressBar dan sembunyikan tombol
            progressBar.setVisibility(View.VISIBLE);
            postStatusButton.setEnabled(false);
            selectImageButton.setEnabled(false);

            // 2. Buat path unik di Firebase Storage untuk setiap gambar
            // Contoh: status_images/userID/randomUUID.jpg
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference()
                    .child("status_images")
                    .child(currentUser.getUid())
                    .child(fileName);

            // 3. Mulai proses upload file
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // 4. Jika upload SUKSES, dapatkan URL download-nya
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            // 5. Jika URL didapatkan, simpan semuanya ke Firestore
                            saveStatusToFirestore(downloadUri.toString(), currentUser);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Jika upload GAGAL
                        Toast.makeText(AddStatusActivity.this, "Upload gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        postStatusButton.setEnabled(true);
                        selectImageButton.setEnabled(true);
                    });
        }
    }

    private void saveStatusToFirestore(String imageUrl, FirebaseUser currentUser) {
        String userId = currentUser.getUid();

        // 1. Ambil data profil dari koleksi "users" terlebih dahulu
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 2. Jika dokumen pengguna ada, ambil nama dan foto profilnya yang asli
                        String userName = documentSnapshot.getString("name");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        // --- Kode di bawah ini adalah bagian yang diperbarui ---

                        // Buat objek Story baru
                        Story newStory = new Story(imageUrl, System.currentTimeMillis());
                        List<Story> stories = new ArrayList<>();
                        stories.add(newStory);

                        // Buat objek Status baru dengan data pengguna yang ASLI
                        Status newStatus = new Status(userId, userName, profileImageUrl, System.currentTimeMillis(), stories);

                        // 3. Simpan objek status yang sudah lengkap ini ke koleksi "statuses"
                        firestore.collection("statuses").document(userId)
                                .set(newStatus, SetOptions.merge()) // Gunakan merge untuk update/create
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddStatusActivity.this, "Status berhasil diposting!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddStatusActivity.this, "Gagal menyimpan status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    postStatusButton.setEnabled(true);
                                    selectImageButton.setEnabled(true);
                                });
                    } else {
                        // Handle jika data pengguna karena suatu alasan tidak ditemukan
                        Toast.makeText(this, "Data profil pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        postStatusButton.setEnabled(true);
                        selectImageButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengambil data profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    postStatusButton.setEnabled(true);
                    selectImageButton.setEnabled(true);
                });
    }
}