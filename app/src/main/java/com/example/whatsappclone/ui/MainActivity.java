package com.example.whatsappclone.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.whatsappclone.R;
import com.example.whatsappclone.models.Call;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging; // <-- Import penting

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Izin notifikasi diberikan.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Anda mungkin tidak akan menerima notifikasi pesan.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNav();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }

        // Jalankan semua fungsi startup
        askNotificationPermission();
        listenForIncomingCalls();
        updateFCMToken();
    }

    private void setupBottomNav() {
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
    }

    // --- METODE BARU UNTUK MENGAMBIL DAN MENYIMPAN TOKEN SECARA PROAKTIF ---
    private void updateFCMToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MainActivity", "Gagal mengambil token FCM.", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("MainActivity", "Token FCM saat ini: " + token);

                    // Simpan token ke dokumen pengguna di Firestore
                    FirebaseFirestore.getInstance().collection("users")
                            .document(currentUser.getUid())
                            .update("fcmToken", token)
                            .addOnSuccessListener(aVoid -> Log.d("MainActivity", "Token FCM berhasil disimpan di Firestore."))
                            .addOnFailureListener(e -> Log.w("MainActivity", "Gagal memperbarui token FCM di Firestore.", e));
                });
    }

    private void listenForIncomingCalls() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        FirebaseFirestore.getInstance().collection("calls")
                .whereEqualTo("receiverId", currentUser.getUid())
                .whereEqualTo("status", "dialing")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { return; }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        Call incomingCall = snapshots.getDocuments().get(0).toObject(Call.class);
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
        } else if (itemId == R.id.action_camera) {
            startActivity(new Intent(this, ContactsActivity.class));
            return true;
        } else if (itemId == R.id.action_logout) {
            signOutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        updateUserStatus("Offline");
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

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
