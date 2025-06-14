package com.example.whatsappclone.main.fragments;

// File: ui/PembaruanFragment.java
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsappclone.status.AddStatusActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.whatsappclone.R; // Ganti dengan package Anda
import com.example.whatsappclone.shared.adapters.StatusAdapter;
import com.example.whatsappclone.shared.models.Status;
import java.util.ArrayList;
import java.util.List;

public class PembaruanFragment extends Fragment {

    private RecyclerView statusRecyclerView;
    private StatusAdapter statusAdapter;
    private List<Status> statusList;

    // Firebase
    private FirebaseFirestore firestore;

    public PembaruanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_updates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        statusRecyclerView = view.findViewById(R.id.rv_status);
        statusRecyclerView.setHasFixedSize(true);
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        statusList = new ArrayList<>();
        statusAdapter = new StatusAdapter(getContext(), statusList);
        statusRecyclerView.setAdapter(statusAdapter);

        // Setup FAB
        view.findViewById(R.id.fab_add_status).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddStatusActivity.class));
        });

        // Panggil metode untuk memuat data asli dari Firestore
        loadStatusesFromFirestore();
    }

    private void loadStatusesFromFirestore() {
        // 1. Dapatkan referensi ke koleksi "statuses"
        // 2. Tambahkan .orderBy() untuk mengurutkan dari yang terbaru
        // 3. Gunakan addSnapshotListener untuk pembaruan real-time
        firestore.collection("statuses")
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w("StatusFragment", "Listen failed.", error);
                        return;
                    }

                    if (snapshots != null) {
                        // 4. Bersihkan list lama agar tidak ada data ganda
                        statusList.clear();
                        // 5. Loop setiap dokumen di dalam snapshot
                        for (Status status : snapshots.toObjects(Status.class)) {
                            // 6. Tambahkan setiap objek status ke dalam list
                            statusList.add(status);
                            Log.d("PembaruanFragment", "Data diterima dari 'statuses': Nama=" + status.getUserName() + ", URL Foto=" + status.getProfileImageUrl());

                        }
                        // 7. Beri tahu adapter bahwa ada data baru untuk ditampilkan
                        statusAdapter.notifyDataSetChanged();
                    }
                });
    }
}