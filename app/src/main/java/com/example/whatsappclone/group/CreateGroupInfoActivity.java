package com.example.whatsappclone.group;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.whatsappclone.R;
import com.example.whatsappclone.main.MainActivity;
import com.example.whatsappclone.shared.models.Group;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupInfoActivity extends AppCompatActivity {

    private CircleImageView groupPhoto;
    private EditText groupName;
    private FloatingActionButton createGroupFab;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    private Uri imageUri;
    private ArrayList<String> memberIds;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    groupPhoto.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create_info);

        initViews();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        memberIds = getIntent().getStringArrayListExtra("SELECTED_MEMBERS");
        // Tambahkan pembuat grup ke dalam daftar anggota
        if (currentUser != null && !memberIds.contains(currentUser.getUid())) {
            memberIds.add(currentUser.getUid());
        }

        groupPhoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        createGroupFab.setOnClickListener(v -> createGroup());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_create_group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        groupPhoto = findViewById(R.id.civ_group_photo);
        groupName = findViewById(R.id.et_group_name);
        createGroupFab = findViewById(R.id.fab_create_group);
        progressBar = findViewById(R.id.progress_bar_create_group);
    }

    private void createGroup() {
        String name = groupName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama grup tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        if (imageUri != null) {
            // Jika ada gambar, unggah dulu
            uploadPhotoAndCreateGroup(name);
        } else {
            // Jika tidak ada gambar, langsung buat grup
            saveGroupToFirestore(name, null);
        }
    }

    private void uploadPhotoAndCreateGroup(String name) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference().child("group_pictures/" + fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveGroupToFirestore(name, uri.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengunggah foto", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void saveGroupToFirestore(String name, String photoUrl) {
        String groupId = UUID.randomUUID().toString();

        Group newGroup = new Group(
                groupId,
                name,
                photoUrl,
                currentUser.getUid(),
                memberIds
        );

        db.collection("groups").document(groupId).set(newGroup)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Grup berhasil dibuat", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    // Nanti kita akan arahkan ke GroupChatActivity
                    finishAffinity(); // Tutup semua activity sebelumnya
                    startActivity(new Intent(this, MainActivity.class)); // Kembali ke halaman utama
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal membuat grup", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            createGroupFab.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            createGroupFab.setVisibility(View.VISIBLE);
        }
    }
}
