package com.example.whatsappclone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsappclone.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // UI Elements
    private SignInButton googleSignInButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Langsung masuk jika pengguna sudah login sebelumnya
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToMainActivity();
            return; // Penting untuk menghentikan eksekusi onCreate lebih lanjut
        }

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi Views
        googleSignInButton = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.login_progress_bar);

        // Konfigurasi Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inisialisasi ActivityResultLauncher modern
        initializeLauncher();

        // Set OnClickListener untuk tombol login
        googleSignInButton.setOnClickListener(v -> signIn());
    }

    private void initializeLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Cek hasil dari intent login Google
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In berhasil, sekarang otentikasi dengan Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) {
                            // Google Sign In gagal
                            Log.w("LoginActivity", "Google sign in failed", e);
                            Toast.makeText(this, "Login Google gagal: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    } else {
                        // Proses dibatalkan atau gagal tanpa exception
                        showLoading(false);
                    }
                }
        );
    }

    private void signIn() {
        showLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in ke Firebase berhasil, simpan data pengguna
                        Log.d("LoginActivity", "Firebase Auth Success");
                        saveUserToFirestore();
                    } else {
                        // Sign in ke Firebase gagal
                        Log.w("LoginActivity", "Firebase Auth Failed", task.getException());
                        Toast.makeText(this, "Otentikasi Firebase gagal", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
    }

    private void saveUserToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showLoading(false);
            return;
        }

        DocumentReference userRef = db.collection("users").document(user.getUid());

        // Siapkan data pengguna dari Akun Google
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUid()); // Menggunakan "userId" agar konsisten
        userData.put("name", user.getDisplayName());
        userData.put("email", user.getEmail());
        if (user.getPhotoUrl() != null) {
            userData.put("profileImageUrl", user.getPhotoUrl().toString()); // Menggunakan "profileImageUrl"
        }

        // Gunakan set dengan SetOptions.merge() untuk membuat dokumen baru atau
        // memperbarui yang sudah ada tanpa menghapus field lain.
        userRef.set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoginActivity", "User data saved/updated in Firestore.");
                    goToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Log.w("LoginActivity", "Error saving user data", e);
                    Toast.makeText(LoginActivity.this, "Gagal menyimpan data pengguna.", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Menutup activity ini agar tidak bisa kembali dengan tombol back
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            googleSignInButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            googleSignInButton.setVisibility(View.VISIBLE);
        }
    }
}
