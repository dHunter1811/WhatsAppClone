package com.example.whatsappclone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.whatsappclone.R;
import com.example.whatsappclone.models.Call; // <-- Tambahkan import ini
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }

        // Listener untuk navigasi
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_chat) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.navigation_updates) {
                selectedFragment = new PembaruanFragment();
            } else if (itemId == R.id.navigation_community) {
                selectedFragment = new KomunitasFragment();
            } else if (itemId == R.id.navigation_calls) {
                selectedFragment = new PanggilanFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Panggil listener untuk panggilan masuk di akhir onCreate
        listenForIncomingCalls();
    }

    // --- BAGIAN KODE BARU UNTUK MENDENGARKAN PANGGILAN MASUK ---
    private void listenForIncomingCalls() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore.getInstance().collection("calls")
                .whereEqualTo("receiverId", currentUser.getUid())
                .whereEqualTo("status", "dialing")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        // Ambil panggilan pertama yang ditemukan
                        Call incomingCall = snapshots.getDocuments().get(0).toObject(Call.class);

                        // Hindari menampilkan layar panggilan jika sudah ada di dalam panggilan
                        // (Logika ini bisa disempurnakan nanti)

                        // Mulai IncomingCallActivity
                        Intent intent = new Intent(MainActivity.this, IncomingCallActivity.class);
                        intent.putExtra("callId", incomingCall.getCallId());
                        intent.putExtra("channelName", incomingCall.getCallId());
                        intent.putExtra("callerName", incomingCall.getCallerName());
                        intent.putExtra("callerPhotoUrl", incomingCall.getCallerPhotoUrl());
                        intent.putExtra("callType", incomingCall.getCallType());
                        startActivity(intent);
                    }
                });
    }


    // --- SISA KODE ANDA YANG SUDAH ADA ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_camera) {
            Toast.makeText(this, "Kamera diklik", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_logout) {
            signOutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        updateUserStatus("Offline"); // Update status jadi offline sebelum logout
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateUserStatus(String status) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
            userRef.update("onlineStatus", status);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("Terakhir aktif: " + DateFormat.format("HH:mm", System.currentTimeMillis()).toString());
    }
}
