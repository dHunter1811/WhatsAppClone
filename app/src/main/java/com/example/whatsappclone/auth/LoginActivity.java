package com.example.whatsappclone.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // <-- Import baru

import com.example.whatsappclone.R;
import com.example.whatsappclone.main.MainActivity;
import com.example.whatsappclone.shared.models.User; // Pastikan import model User benar
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // PERUBAHAN: Mengganti SignInButton dengan CardView
    private CardView googleLoginButtonContainer;
    private ProgressBar progressBar;

    private static final String TAG = "LoginActivityDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToMainActivity();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // PERUBAHAN: Inisialisasi tombol kustom baru
        googleLoginButtonContainer = findViewById(R.id.google_login_button_container);
        progressBar = findViewById(R.id.login_progress_bar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeLauncher();

        // PERUBAHAN: Set listener pada tombol kustom
        googleLoginButtonContainer.setOnClickListener(v -> signIn());
    }

    private void initializeLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "Google Sign In Berhasil. Memulai otentikasi Firebase.");
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) {
                            Log.e(TAG, "Login Google Gagal", e);
                            Toast.makeText(this, "Login Google gagal: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    } else {
                        Log.w(TAG, "Login Google dibatalkan atau gagal. Result Code: " + result.getResultCode());
                        showLoading(false);
                    }
                }
        );
    }

    private void signIn() {
        Log.d(TAG, "Tombol sign in diklik, memulai intent Google.");
        showLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "Mendapatkan kredensial dari akun Google.");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Otentikasi Firebase BERHASIL. Memulai penyimpanan ke Firestore.");
                        saveUserToFirestore();
                    } else {
                        Log.e(TAG, "Otentikasi Firebase GAGAL", task.getException());
                        Toast.makeText(this, "Otentikasi Firebase gagal.", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
    }

    private void saveUserToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "saveUserToFirestore dipanggil tetapi user null!");
            showLoading(false);
            return;
        }

        String userId = user.getUid();
        String name = user.getDisplayName();
        String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

        Log.d(TAG, "Data yang akan disimpan: UID=" + userId + ", Nama=" + name + ", FotoURL=" + photoUrl);

        // Menggunakan model User untuk memastikan konsistensi data
        User userData = new User(userId, name, photoUrl);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Data pengguna BERHASIL disimpan ke Firestore.");
                    goToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Data pengguna GAGAL disimpan ke Firestore.", e);
                    Toast.makeText(LoginActivity.this, "Gagal menyimpan profil.", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Menutup activity ini agar tidak bisa kembali dengan tombol back
    }

    // PERUBAHAN: Mengatur visibilitas untuk tombol kustom
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            googleLoginButtonContainer.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            googleLoginButtonContainer.setVisibility(View.VISIBLE);
        }
    }
}
