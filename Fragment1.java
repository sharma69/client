package com.example.chatit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
public class Fragment1  extends Fragment {

    RecyclerView recyclerView;
    FirebaseDatabase database ;
    DatabaseReference chatlist,lastseenref;
    String password,currentuid;
    FirebaseFirestore db;
    DocumentReference documentReference;
    FirebaseAuth mAuth;
    Superclass superclass;
    DatabaseReference statusRef,laststatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment1,container,false);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getActivity().findViewById(R.id.rv_f1);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        database = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user =  mAuth.getCurrentUser();
        currentuid = user.getUid();

        chatlist = database.getReference("chat list").child(currentuid);
        documentReference = db.collection("user").document(currentuid);
        lastseenref = database.getReference("online");

        statusRef = database.getReference("Status");

        lastseenref.child(currentuid).setValue("online");

        checkAccount();
        deletestatus();
    }

    private void deletestatus() {

//        if (System.currentTimeMillis() >= delete) {
//
//            Query query = statusRef.orderByChild("delete").equalTo(delete);
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//
//                        snapshot1.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//
//
//        } else {
//        }
    }
    private void checkAccount() {

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()){

                          //  Toast.makeText(getActivity(), "profile exist", Toast.LENGTH_SHORT).show();
                        }else {

                            Intent intent = new Intent(getActivity(),Profile.class);
                            startActivity(intent);


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        checkAccount();

        FirebaseRecyclerOptions<ListModal> options =
                new FirebaseRecyclerOptions.Builder<ListModal>()
                        .setQuery(chatlist,ListModal.class)
                        .build();

        FirebaseRecyclerAdapter<ListModal,ListVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ListModal, ListVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ListVH holder,
                                                    int position, @NonNull ListModal model) {

                        holder.setList(getActivity(),model.getTime(),model.getLastm(),
                                model.getName()
                                ,model.getUrl(),model.getUid());

                        String postkey = getRef(position).getKey();
                        String ruid = getItem(position).getUid();
                        String name = getItem(position).getName();
                        String urlofuser = getItem(position).getUrl();


                        holder.checksendermcount(currentuid,ruid);
                        holder.nametv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                checkLockstatus(ruid);
                            }
                        });
                        holder.lastmtv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                checkLockstatus(ruid);
                            }
                        });

                        holder.nametv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {

                                showchatlockBS(name,ruid);

                                return false;

                            }
                        });
                        holder.listiv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                showdp(urlofuser);
                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item,parent,false);

                        return new ListVH(view);

                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void checkLockstatus(String ruid) {

        DatabaseReference chatlockref = database.getReference("Chat lock");

        chatlockref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(currentuid+ruid).hasChild("locked")){
                    showpassdialog(ruid);
                }else {

                    Intent intent = new Intent(getActivity(),MessageAct.class);
                    intent.putExtra("ruid",ruid);
                    intent.putExtra("stype","yes");
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showdp(String urlofuser) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dp_show,null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView imageView = view.findViewById(R.id.dp_iv);


        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        alertDialog.show();

        Picasso.get().load(urlofuser).into(imageView);

    }

    private void showpassdialog(String ruid) {

        LayoutInflater inflater  = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.pass_lc,null);
        EditText noteEt = view.findViewById(R.id.edit_note);
        Button button = view.findViewById(R.id.btn_edit);

        DatabaseReference chatlockref = database.getReference("Chat lock");


        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        alertDialog.show();

        chatlockref.child(currentuid+ruid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

               password = snapshot.child("locked").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = Decode.decode(password);

                if (key.equals(noteEt.getText().toString().trim())){

                    Intent i = new Intent(getActivity(),MessageAct.class);
                    i.putExtra("ruid",ruid);
                    i.putExtra("stype","yes");
                    startActivity(i);
                    alertDialog.dismiss();
                }else {
                    Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void showchatlockBS(String name, String ruid) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chatlock_bs);


        EditText passEt = dialog.findViewById(R.id.passetLC);
        TextView nametv = dialog.findViewById(R.id.unamelc);
        TextView savetv = dialog.findViewById(R.id.savetvlc);
        TextView canceltv = dialog.findViewById(R.id.canceltvlc);

        nametv.setText(name);

        savetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password =  Encode.encode(passEt.getText().toString().trim());

                DatabaseReference chatlockref = database.getReference("Chat lock").child(currentuid+ruid);

                chatlockref.child("locked").setValue(password);

                dialog.dismiss();
                Toast.makeText(getActivity(), "Chat locked", Toast.LENGTH_SHORT).show();
            }
        });



        canceltv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lastseenref.child(currentuid).setValue("offline");
    }
}
