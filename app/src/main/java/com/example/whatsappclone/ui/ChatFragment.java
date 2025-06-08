package com.example.whatsappclone.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.whatsappclone.R;
import com.example.whatsappclone.adapters.ChatAdapter;
import com.example.whatsappclone.adapters.UserAdapter;
import com.example.whatsappclone.models.ChatItem;
import com.example.whatsappclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private List<ChatItem> chatList;
    private ChatAdapter chatAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // penting agar fragment menerima event menu
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList, chatItem -> {
            Intent intent = new Intent(requireActivity(), ChatActivity.class);
            intent.putExtra("chatName", chatItem.getName());
            intent.putExtra("chatUid", chatItem.getUid());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        ImageView userPhoto = view.findViewById(R.id.userPhoto);
        TextView userName = view.findViewById(R.id.userName);

        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String name = snapshot.getString("name");
                            String photoUrl = snapshot.getString("photoUrl");

                            userName.setText(name);

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Picasso.get().load(photoUrl).into(userPhoto);
                            }
                        }
                    });
        }

        SearchView searchView = view.findViewById(R.id.searchView);
        CardView searchCard = view.findViewById(R.id.searchCard);

        searchCard.setOnClickListener(v -> {
            searchView.requestFocus();
            searchView.setIconified(false);
            showKeyboard(searchView);
        });

        loadChatsFromFirebase();
        loadUserList(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu); // inflate menu dari res/menu/toolbar_menu.xml
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_more) {
            // Logout dan arahkan ke LoginActivity
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // hapus backstack
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChatsFromFirebase() {
        if (currentUser == null) return;

        chatList.clear();

        db.collection("users").get().addOnSuccessListener(userSnapshot -> {
            for (DocumentSnapshot userDoc : userSnapshot.getDocuments()) {
                String uid = userDoc.getId();

                if (uid.equals(currentUser.getUid())) continue;

                String name = userDoc.getString("name");
                String photoUrl = userDoc.getString("photoUrl");

                String chatId = generateChatId(currentUser.getUid(), uid);

                db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(messageSnapshot -> {
                            String lastMessage = "";
                            long lastMessageTime = 0;

                            if (!messageSnapshot.isEmpty()) {
                                DocumentSnapshot msgDoc = messageSnapshot.getDocuments().get(0);
                                lastMessage = msgDoc.getString("message");
                                Long ts = msgDoc.getLong("timestamp");
                                if (ts != null) lastMessageTime = ts;
                            }

                            ChatItem chatItem = new ChatItem(uid, name, photoUrl, lastMessage, lastMessageTime);
                            chatList.add(chatItem);

                            chatList.sort((a, b) -> Long.compare(b.getLastMessageTime(), a.getLastMessageTime()));

                            chatAdapter.notifyDataSetChanged();
                        });
            }
        });
    }

    private String generateChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void loadUserList(View view) {
        List<User> userList = new ArrayList<>();
        UserAdapter userAdapter = new UserAdapter(getContext(), userList);

        RecyclerView recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUsers.setAdapter(userAdapter);

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (!doc.getId().equals(currentUser.getUid())) {
                            String name = doc.getString("name");
                            String photoUrl = doc.getString("photoUrl");
                            String uid = doc.getId();

                            userList.add(new User(uid, name, photoUrl));
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                });
    }

    private void showKeyboard(SearchView searchView) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT);
    }
}
