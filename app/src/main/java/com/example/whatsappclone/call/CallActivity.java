package com.example.whatsappclone.call;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.models.Call;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

import com.example.whatsappclone.shared.models.CallHistory;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    // UI Views
    private FrameLayout remoteVideoContainer, localVideoContainer;
    private ImageButton btnEndCall, btnMute, btnToggleCamera, btnSwitchCamera;

    // Agora
    private RtcEngine agoraEngine;
    private String channelName;
    private String callId;

    // State
    private boolean isMuted = false;
    private boolean isVideoDisabled = false;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> remoteVideoContainer.removeAllViews());
        }
    };

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {
                boolean allPermissionsGranted = true;
                for (Boolean granted : isGranted.values()) {
                    allPermissionsGranted &= granted;
                }
                if (allPermissionsGranted) {
                    initializeAndJoinChannel();
                } else {
                    Toast.makeText(this, "Izin kamera dan mikrofon diperlukan.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_main);

        initViews();
        setupClickListeners();

        channelName = getIntent().getStringExtra("channelName");
        callId = getIntent().getStringExtra("callId");

        if (checkPermissions()) {
            initializeAndJoinChannel();
        } else {
            requestPermissionsLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
        }
    }

    private void initViews() {
        remoteVideoContainer = findViewById(R.id.remote_video_view_container);
        localVideoContainer = findViewById(R.id.local_video_view_container);
        btnEndCall = findViewById(R.id.btn_end_call);
        btnMute = findViewById(R.id.btn_mute);
        btnToggleCamera = findViewById(R.id.btn_toggle_camera);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
    }

    private void setupClickListeners() {
        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> onMuteClicked());
        btnToggleCamera.setOnClickListener(v -> onToggleCameraClicked());
        btnSwitchCamera.setOnClickListener(v -> onSwitchCameraClicked());
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeAndJoinChannel() {
        try {
            // PERBAIKAN: Menggunakan RtcEngineConfig sesuai API v4
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = getString(R.string.agora_app_id);
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        } catch (Exception e) {
            Log.e("CallActivity", "Error initializing Agora: ", e);
            throw new RuntimeException("Gagal menginisialisasi Agora Engine: " + e.toString());
        }

        agoraEngine.enableVideo();
        setupLocalVideo();

        // PERBAIKAN: Menggunakan ChannelMediaOptions sesuai API v4
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

        agoraEngine.joinChannel(null, channelName, 0, options);
    }

    private void setupLocalVideo() {
        localVideoContainer.removeAllViews();
        // PERBAIKAN: Buat SurfaceView menggunakan cara standar Android
        SurfaceView surfaceView = new SurfaceView(this);
        localVideoContainer.addView(surfaceView);
        agoraEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        agoraEngine.startPreview();
    }

    private void setupRemoteVideo(int uid) {
        remoteVideoContainer.removeAllViews();
        // PERBAIKAN: Buat SurfaceView menggunakan cara standar Android
        SurfaceView surfaceView = new SurfaceView(this);
        remoteVideoContainer.addView(surfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void onMuteClicked() {
        isMuted = !isMuted;
        agoraEngine.muteLocalAudioStream(isMuted);
        btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
    }

    private void onToggleCameraClicked() {
        isVideoDisabled = !isVideoDisabled;
        agoraEngine.enableLocalVideo(!isVideoDisabled);
        btnToggleCamera.setImageResource(isVideoDisabled ? R.drawable.ic_action_videocam_off : R.drawable.ic_action_videocam);
    }

    private void onSwitchCameraClicked() {
        agoraEngine.switchCamera();
    }

    private void endCall() {
        leaveChannel();

        if (callId != null) {
            // 1. Update status panggilan menjadi "ended"
            DocumentReference callRef = FirebaseFirestore.getInstance().collection("calls").document(callId);
            callRef.update("status", "ended");

            // 2. Ambil detail panggilan untuk disimpan ke riwayat
            callRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Call callData = documentSnapshot.toObject(Call.class);
                    if (callData != null) {
                        saveCallToHistory(callData);
                    }
                }
            });
        }
        finish(); // Tutup activity setelah semuanya dimulai
    }

    private void saveCallToHistory(Call callData) {
        // Selalu buat entri riwayat untuk Penelepon (siapa pun yang memulai panggilan)
        String callerHistoryId = UUID.randomUUID().toString();
        CallHistory callerHistory = new CallHistory(
                callerHistoryId,
                callData.getReceiverId(),
                null, // Nama receiver akan diambil nanti di adapter
                null, // Foto receiver akan diambil nanti di adapter
                System.currentTimeMillis(),
                callData.getCallType(),
                "outgoing"
        );
        FirebaseFirestore.getInstance().collection("users").document(callData.getCallerId())
                .collection("call_history").document(callerHistoryId).set(callerHistory);

        // --- PERBAIKAN PENTING DI SINI ---
        // Hanya buat entri riwayat untuk Penerima jika ia BUKAN orang yang sama
        if (!callData.getCallerId().equals(callData.getReceiverId())) {
            String receiverHistoryId = UUID.randomUUID().toString();
            CallHistory receiverHistory = new CallHistory(
                    receiverHistoryId,
                    callData.getCallerId(),
                    callData.getCallerName(),
                    callData.getCallerPhotoUrl(),
                    System.currentTimeMillis(),
                    callData.getCallType(),
                    "incoming"
            );
            FirebaseFirestore.getInstance().collection("users").document(callData.getReceiverId())
                    .collection("call_history").document(receiverHistoryId).set(receiverHistory);
        }
    }

    private void leaveChannel() {
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (agoraEngine != null) {
            agoraEngine.stopPreview();
            leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }
    }
}
