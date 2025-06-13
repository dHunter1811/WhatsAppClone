package com.example.whatsappclone.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatsappclone.R;
import com.example.whatsappclone.shared.adapters.GroupAdapter;
import com.example.whatsappclone.shared.models.Group;
import com.example.whatsappclone.group.GroupChatActivity;
import com.example.whatsappclone.group.SelectMembersActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class KomunitasFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Tambahkan TAG untuk logging
    private static final String TAG = "KomunitasFragmentDebug";

    public KomunitasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.rv_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        adapter = new GroupAdapter(getContext(), groupList, group -> {
            Intent intent = new Intent(getActivity(), GroupChatActivity.class);
            intent.putExtra("GROUP_ID", group.getGroupId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabNewGroup = view.findViewById(R.id.fab_new_group);
        fabNewGroup.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SelectMembersActivity.class));
        });

        loadGroups();
    }

    private void loadGroups() {
        if (currentUser == null) {
            Log.d(TAG, "Pengguna belum login, loadGroups dihentikan.");
            return;
        }

        Log.d(TAG, "Mulai memuat grup untuk pengguna: " + currentUser.getUid());

        db.collection("groups")
                .whereArrayContains("participants", currentUser.getUid())
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    // --- PERHATIKAN LOG DI BAGIAN INI ---
                    if (error != null) {
                        Log.e(TAG, "Gagal mendengarkan daftar grup. Error: ", error);
                        // JIKA ANDA MELIHAT ERROR INI, SOLUSINYA ADA DI BAWAH
                        return;
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "Query berhasil, ditemukan " + snapshots.size() + " grup.");
                        groupList.clear();
                        groupList.addAll(snapshots.toObjects(Group.class));
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Adapter diperbarui dengan " + groupList.size() + " grup.");
                    } else {
                        Log.d(TAG, "Snapshot null, tidak ada data grup.");
                    }
                });
    }
}
