package com.example.whatsappclone.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.R; // Ganti dengan package Anda
import com.example.whatsappclone.models.Status;
import com.example.whatsappclone.models.Story;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StatusViewerActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    // Konstanta
    private static final long STORY_DURATION = 5000L; // 5 detik

    // Views
    private StoriesProgressView storiesProgressView;
    private ImageView storyImageView;
    private CircleImageView profileImageViewer;
    private TextView userNameViewer;
    private View rootView; // Root view untuk mendeteksi sentuhan

    // Data dan State
    private List<Story> stories;
    private int storyCounter = 0;
    private long pressTime = 0L;
    private long limit = 500L;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_viewer);

        // Inisialisasi Views
        storiesProgressView = findViewById(R.id.stories_progress_view);
        storyImageView = findViewById(R.id.iv_story_image);
        profileImageViewer = findViewById(R.id.iv_profile_pic_viewer);
        userNameViewer = findViewById(R.id.tv_user_name_viewer);
        rootView = findViewById(R.id.root_view);
        findViewById(R.id.iv_close_viewer).setOnClickListener(v -> finish()); // Tombol close

        // Ambil objek Status
        Status status = (Status) getIntent().getSerializableExtra("status_object");

        if (status != null) {
            userNameViewer.setText(status.getUserName());
            Glide.with(this).load(status.getProfileImageUrl()).into(profileImageViewer);
            stories = status.getStories();
        }

        // Setup StoriesProgressView
        storiesProgressView.setStoriesCount(stories.size());
        storiesProgressView.setStoryDuration(STORY_DURATION);
        storiesProgressView.setStoriesListener(this);
        storiesProgressView.startStories(storyCounter); // Mulai dari story pertama

        // Tampilkan gambar pertama
        showStory(storyCounter);

        // Setup listener sentuhan untuk pause, resume, prev, next
        View.OnTouchListener touchListener = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        };

        // Listener untuk skip dan reverse
        findViewById(R.id.iv_story_image).setOnTouchListener(touchListener);
    }

    private void showStory(int index) {
        if (stories != null && index >= 0 && index < stories.size()) {
            Glide.with(this).load(stories.get(index).getImageUrl()).into(storyImageView);
        }
    }

    // -- Implementasi dari StoriesListener --

    @Override
    public void onNext() {
        // Pindah ke story berikutnya
        if (storyCounter < stories.size()) {
            storyCounter++;
            showStory(storyCounter);
        }
    }

    @Override
    public void onPrev() {
        // Pindah ke story sebelumnya
        if (storyCounter > 0) {
            storyCounter--;
            storiesProgressView.startStories(storyCounter); // Restart progress dari story sebelumnya
            showStory(storyCounter);
        }
    }

    @Override
    public void onComplete() {
        // Semua story sudah selesai, tutup activity
        finish();
    }

    @Override
    protected void onDestroy() {
        // Hancurkan progress view untuk menghindari memory leak
        storiesProgressView.destroy();
        super.onDestroy();
    }
}