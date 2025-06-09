package com.example.whatsappclone.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.CallHistoryAdapter;
import com.example.whatsappclone.models.CallHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class PanggilanFragment extends Fragment {

    private RecyclerView recyclerView;
    private CallHistoryAdapter adapter;
    private List<CallHistory> callHistoryList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public PanggilanFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_panggilan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.rv_call_history); // Pastikan ID ini ada di layout Anda
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        callHistoryList = new ArrayList<>();
        adapter = new CallHistoryAdapter(getContext(), callHistoryList);
        recyclerView.setAdapter(adapter);

        loadCallHistory();
    }

    private void loadCallHistory() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).collection("call_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (snapshots != null) {
                        callHistoryList.clear();
                        callHistoryList.addAll(snapshots.toObjects(CallHistory.class));
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
