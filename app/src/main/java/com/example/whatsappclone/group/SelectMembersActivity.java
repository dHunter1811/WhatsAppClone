package com.example.whatsappclone.group;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.adapters.SelectableUserAdapter;
import com.example.whatsappclone.shared.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SelectMembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SelectableUserAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create_select);

        Toolbar toolbar = findViewById(R.id.toolbar_select_members);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.rv_select_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new SelectableUserAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        loadUsers();

        FloatingActionButton fab = findViewById(R.id.fab_next_step);
        fab.setOnClickListener(v -> {
            List<User> selectedUsers = adapter.getSelectedUsers();
            if (selectedUsers.isEmpty()) {
                Toast.makeText(this, "Pilih minimal satu anggota", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> selectedUserIds = new ArrayList<>();
            for (User user : selectedUsers) {
                selectedUserIds.add(user.getUserId());
            }

            // Pindah ke activity berikutnya untuk memberi info grup
            Intent intent = new Intent(this, CreateGroupInfoActivity.class);
            intent.putStringArrayListExtra("SELECTED_MEMBERS", selectedUserIds);
            startActivity(intent);
        });
    }

    private void loadUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Jangan tampilkan diri sendiri di daftar pilihan
                    if (!document.getId().equals(currentUser.getUid())) {
                        User user = document.toObject(User.class);
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Gagal memuat pengguna", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
