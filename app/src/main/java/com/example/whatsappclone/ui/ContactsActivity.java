package com.example.whatsappclone.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.UserAdapter;
import com.example.whatsappclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Toolbar toolbar = findViewById(R.id.toolbar_contacts);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        FirebaseFirestore.getInstance().collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.getId().equals(currentUserId)) {
                                User user = document.toObject(User.class);
                                userList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Gagal memuat kontak.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
