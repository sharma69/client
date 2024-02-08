package com.example.chatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShowUsers extends AppCompatActivity {

    RecyclerView recyclerView;
    DocumentReference documentReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentUserId,seentype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        recyclerView = findViewById(R.id.rv_alluser);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();
        documentReference = db.collection("user").document(currentUserId);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = db.collection("user");

        FirestoreRecyclerOptions<UserModal> options =
                new FirestoreRecyclerOptions.Builder<UserModal>()
                .setQuery(query,UserModal.class)
                .build();

        FirestoreRecyclerAdapter firestoreRecyclerAdapter =
                new FirestoreRecyclerAdapter<UserModal,UserVH>(options){

                    @NonNull
                    @Override
                    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.user_item,parent,false);

                        return new UserVH(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull UserVH holder, int position, @NonNull UserModal model) {

                        holder.setUser(getApplication(),model.getName(),model.getPhone(),
                                model.getAbout(),model.getUrl(),model.getUid());

                        String uid = getItem(position).getUid();
                        String name = getItem(position).getName();
                        String phone = getItem(position).getPhone();
                        String url = getItem(position).getUrl();

                        holder.nametc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent i = new Intent(ShowUsers.this,MessageAct.class);
                                i.putExtra("ruid",uid);
                                i.putExtra("stype","no");
                                startActivity(i);
                            }
                        });

                    }
                };

        firestoreRecyclerAdapter.startListening();
        recyclerView.setAdapter(firestoreRecyclerAdapter);
    }
}