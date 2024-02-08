package com.example.chatit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.squareup.picasso.Picasso;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAct extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {


    EditText messageEt,searchEt;
    TextView nametv, lasteentv, replyinto_nametv,replyingto_mtv;
    ImageButton moreBtn, sendbtn, imgbtn,cancelreplyingtobtn;
    RecyclerView recyclerViewm;
    private Uri imageUri;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference sRef, rRef, schatlist, rchatlist, lastseenref, groupRef, groupchat,
            imageDownload, reactionsref,seenStatus,checkingchatref;
    String rname, rurl, rabout, rphone, suid="", ruid = "", sname, sabout, sphone,
            surl, address, usertoken, savetime,rcCode,messageSMS,seentype = "",typingstatus,filename;
    ImageView imageView;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    MessageModal modal, rmodal;
    ListModal listModel, rlistmodel;
    DocumentReference documentReference, documentReferenceSender;
    FirebaseFirestore db;
    final String delete = String.valueOf(System.currentTimeMillis());

    // reference for image forward
    FirebaseStorage storage = FirebaseStorage.getInstance();
    GroupChatModal messageModal;

    LastmModal lastmModal ;
    DatabaseReference lastmREf;
  // yourAppSign
    ZegoSendCallInvitationButton videocall,audiocall;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        db = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getInstance().getReference("chat images");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        suid = user.getUid();


        imageView = findViewById(R.id.iv_message);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            seentype = extras.getString("stype");
            ruid = extras.getString("ruid");
        }

        messageEt = findViewById(R.id.et_message);
        searchEt = findViewById(R.id.search_m_et);
        nametv = findViewById(R.id.nametvmessage);
        moreBtn = findViewById(R.id.morebtn);
        sendbtn = findViewById(R.id.sendbtn);
        imgbtn = findViewById(R.id.imgbtn);
        recyclerViewm = findViewById(R.id.rv_message);
        lasteentv = findViewById(R.id.lastseenmtv);

        cancelreplyingtobtn = findViewById(R.id.cancel_replybtn);
        replyingto_mtv = findViewById(R.id.replyingmessagetotv);
        replyinto_nametv = findViewById(R.id.replyingtotv);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerViewm.setLayoutManager(manager);
        recyclerViewm.setHasFixedSize(true);
        audiocall = findViewById(R.id.audiocallbtn);
        videocall = findViewById(R.id.videocallbtn);
        imgbtn = findViewById(R.id.imgbtn);

        // all models class initialization

        listModel = new ListModal();
        rlistmodel = new ListModal();
        modal = new MessageModal();
        rmodal = new MessageModal();

        // group chat models
        lastmModal = new LastmModal();
        messageModal = new GroupChatModal();
       
        documentReferenceSender = db.collection("user").document(suid);
        documentReference = db.collection("user").document(ruid);

        documentReferenceSender.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()) {

                            sname = task.getResult().getString("name");
                            sabout = task.getResult().getString("about");
                            sphone = task.getResult().getString("phone");
                            surl = task.getResult().getString("url");

                        } else {
                            Toast.makeText(MessageAct.this, "No Profile exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MessageAct.this, e + "", Toast.LENGTH_SHORT).show();
                    }
                });


        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()) {

                            rname = task.getResult().getString("name");
                            rabout = task.getResult().getString("about");
                            rphone = task.getResult().getString("phone");
                            rurl = task.getResult().getString("url");

                            nametv.setText(rname);
                            Picasso.get().load(rurl).into(imageView);

                        } else {
                            Toast.makeText(MessageAct.this, "No Profile exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MessageAct.this, e + "", Toast.LENGTH_SHORT).show();
                    }
                });

        imageDownload = database.getReference("download");
        reactionsref = database.getReference("reactions");

        storageReference = storage.getReference("gcimages");

        // realtime data base
        sRef = database.getReference("Message").child(suid).child(ruid);
        rRef = database.getReference("Message").child(ruid).child(suid);
        schatlist = database.getReference("chat list").child(suid);
        checkingchatref = database.getReference("chat list");
        rchatlist = database.getReference("chat list").child(ruid);
        lastseenref = database.getReference("online");

        seenStatus = database.getReference("seenstatus");

        seenStatus.child(ruid+suid).child("counts").removeValue();

        Calendar time1 = Calendar.getInstance();
        SimpleDateFormat currenttime = new
                SimpleDateFormat("HH:mm:ss a");
        savetime = currenttime.format(time1.getTime());

//        getdata();
//        onlineuser();

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(MessageAct.this,view);
                popupMenu.setOnMenuItemClickListener(MessageAct.this);
                popupMenu.inflate(R.menu.menumessage);
                popupMenu.show();


            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = messageEt.getText().toString().trim();
                messageSMS = messageEt.getText().toString().trim();

                if (message.isEmpty()) {
                    Toast.makeText(MessageAct.this, "cannot send empty message", Toast.LENGTH_SHORT).show();
                } else {

                    String encodemessage = Encode.encode(message);

                    // set the code to the edit text

                    modal.setMessage(encodemessage);
                    modal.setSearch(encodemessage);
                    modal.setRuid(ruid);
                    modal.setSuid(suid);
                    modal.setTime(savetime);
                    modal.setType("txt");
                    modal.setReaction(9);
                    modal.setReactionrec(9);
                    modal.setMno("0");
                    String key = sRef.push().getKey();
                    modal.setDelete(key);

                    sRef.child(key).setValue(modal);

                    rmodal.setMessage(encodemessage);
                    rmodal.setSearch(encodemessage);
                    rmodal.setRuid(ruid);
                    rmodal.setSuid(suid);
                    rmodal.setType("txt");
                    rmodal.setTime(savetime);
                    rmodal.setMno("");
                    rmodal.setReaction(9);
                    rmodal.setReactionrec(9);


                    String key2 = rRef.push().getKey();
                    rmodal.setDelete(key2);

                    rRef.child(key2).setValue(rmodal);

                    // chat list ref

                    listModel.setLastm(encodemessage);
                    listModel.setName(rname);
                    listModel.setUid(ruid);
                    listModel.setUrl(rurl);
                    listModel.setTime(savetime);

                    schatlist.child(ruid).setValue(listModel);


                    // adding data in receiver

                    rlistmodel.setLastm(encodemessage);
                    rlistmodel.setName(sname);
                    rlistmodel.setUid(suid);
                    rlistmodel.setUrl(surl);
                    rlistmodel.setTime(savetime);
                    rchatlist.child(suid).setValue(rlistmodel);

                    // sender status
                    seenStatus.child(suid+ruid).child("counts").child(message).setValue("sent");


                }
                sendNotification(suid, ruid);
            }
        });

        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intenth = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intenth.setType("image/*");
                startActivityForResult(intenth,1);

            }
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            // when there is no text added
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() == 0) {
                    // set text to Not typing
                    lastseenref.child(suid).setValue("Online ");
                    typingstatus = "1";
                } else {
                    // set text to typing
                    lastseenref.child(suid).setValue("is typing ");
                    typingstatus = "1";
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastseenref.child(suid).setValue("is typing ");
                typingstatus = "1";
            }

            // after we input some text
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    // set text to Stopped typing
                    lastseenref.child(suid).setValue("Online ");
                }
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    search();
                }catch (Exception e){

                }

            }
        });

        nametv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageAct.this,Showprofile.class);
                intent.putExtra("uid",ruid);
                startActivity(intent);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageAct.this,Showprofile.class);
                intent.putExtra("uid",ruid);
                startActivity(intent);
            }
        });

        Handler handler5 = new Handler();
        handler5.postDelayed(new Runnable() {
            @Override
            public void run() {
                voicecall();
                videocall();
                intializecall(sphone,sname);
            }
        },1500);

        // need a activityContext.
//        PermissionX.init(this).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
//                .onExplainRequestReason(new ExplainReasonCallback() {
//                    @Override
//                    public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
//                        String message = "We need your consent for the following permissions in order to use the offline call function properly";
//                        scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny");
//                    }
//                }).request(new RequestCallback() {
//                    @Override
//                    public void onResult(boolean allGranted, @NonNull List<String> grantedList,
//                                         @NonNull List<String> deniedList) {
//                    }
//                });
    }
    void voicecall(){
        audiocall.setIsVideoCall(false);
        audiocall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        audiocall.setInvitees(Collections.singletonList(new ZegoUIKitUser(rphone,rname)));
    }

    void videocall(){
        videocall.setIsVideoCall(true);
        videocall.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        videocall.setInvitees(Collections.singletonList(new ZegoUIKitUser(rphone,rname)));
    }

    void intializecall(String suid,String sname){

        long appID = 1443544219 ;   // yourAppID
        String appSign = "7b29d457fb5c47ea7f2cda280ed1aa0096f79326ea0688ce48884e82a5f9597c";
      //  Application application = new Application() ; // Android's application context

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallInvitationService.init(getApplication(), appID, appSign, suid, sname,callInvitationConfig);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

     //   ZegoUIKitPrebuiltCallInvitationService.unInit();
    }

    private void search() {

        String query = searchEt.getText().toString();
        String decodem = Encode.encode(query);
        Query search = sRef.orderByChild("search").startAt(decodem).endAt(decodem+"\uf0ff");

        FirebaseRecyclerOptions<MessageModal> options =
                new FirebaseRecyclerOptions.Builder<MessageModal>()
                        .setQuery(search,MessageModal.class)
                        .build();

        FirebaseRecyclerAdapter<MessageModal,MessageVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<MessageModal, MessageVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MessageVH holder, int position, @NonNull MessageModal model) {

                        holder.setmessage(getApplication(),model.getMessage(),model.getSearch(),model.getDelete(),model.getTime()
                                ,model.getSuid(),model.getRuid(),model.getUrl(),model.getType(),model.getMno(),model.getReaction(),model.getReactionrec());

                    }

                    @NonNull
                    @Override
                    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.message_item,parent,false);

                        return new MessageVH(view);

                    }
                };

        recyclerViewm.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){

            case R.id.searchm:
                lasteentv.setVisibility(View.GONE);
                nametv.setVisibility(View.GONE);
                searchEt.setVisibility(View.VISIBLE);

                return true;

            case R.id.clearchatm:
              AlertDialog.Builder builder = new AlertDialog.Builder(MessageAct.this);
              builder.setTitle("Delete message")
                      .setMessage("Are you sure to clear chat")
                      .setPositiveButton("Delete for me", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {

                              sRef.removeValue();
                          }
                      })
                      .setNegativeButton("Delete for both", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              sRef.removeValue();
                              rRef.removeValue();
                          }
                      })
                      .setNeutralButton("No", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              dialogInterface.dismiss();
                          }
                      });

              builder.create();
              builder.show();

                return true;

            case R.id.optionm:
                try {
                    lockstatus();
                }catch (Exception e){
                    Toast.makeText(this, "Processing", Toast.LENGTH_SHORT).show();
                }
                return true;

        }

        return false;
    }

    private void lockstatus() {
        DatabaseReference chatlockref = database.getReference("Chat lock");

        chatlockref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(suid+ruid).hasChild("locked")){

                    AlertDialog.Builder builder = new AlertDialog.Builder(MessageAct.this);
                    builder.setTitle("UnLock Chat")
                            .setMessage("Are you sure to UnLock chat")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                 chatlockref.child(suid+ruid).removeValue();
                                    Toast.makeText(MessageAct.this, "Unlocked", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();
                                }
                            });

                    builder.create();
                    builder.show();
                }else {
                    showChatlockbS(rname,ruid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showChatlockbS(String rname, String ruid) {
        final Dialog dialog = new Dialog(MessageAct.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chatlock_bs);


        EditText passEt = dialog.findViewById(R.id.passetLC);
        TextView nametv = dialog.findViewById(R.id.unamelc);
        TextView savetv = dialog.findViewById(R.id.savetvlc);
        TextView canceltv = dialog.findViewById(R.id.canceltvlc);

        nametv.setText(rname);

        savetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password =  Encode.encode(passEt.getText().toString().trim());

                DatabaseReference chatlockref = database.getReference("Chat lock").child(suid+ruid);

                chatlockref.child("locked").setValue(password);

                dialog.dismiss();
                Toast.makeText(MessageAct.this, "Chat Locked", Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==1 || resultCode == RESULT_OK || data != null || data.getData() != null){
            imageUri = data.getData();

            previewIMG(imageUri);
            convertMediaUriToPath(imageUri);
            Cursor returnCursor =
                    getContentResolver().query(imageUri, null, null,
                            null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
          //  pickidtv.setText(returnCursor.getString(nameIndex));

        //     filename === returnCursor.getString(nameIndex);

        }else {

        }}

    public String convertMediaUriToPath(Uri imageUri) {

        String [] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(imageUri,proj,null,null,null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();

        return path;
    }

    private void previewIMG(Uri imageUri) {

        LayoutInflater inflater = LayoutInflater.from(MessageAct.this);
        View view = inflater.inflate(R.layout.draw_preview,null);

        ImageView imageView = view.findViewById(R.id.preview_iv);
        Button removebtn = view.findViewById(R.id.preview_removebtn);
        Button uploadbtn = view.findViewById(R.id.preview_uploadbtn);
        ProgressBar pb = view.findViewById(R.id.pb_preview);
        EditText edittext = view.findViewById(R.id.et_preview);


        AlertDialog alertDialog = new AlertDialog.Builder(MessageAct.this)
                .setView(view)
                .create();

        alertDialog.show();

        Picasso.get().load(imageUri).into(imageView);

        removebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
            }
        });

        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String imagename = edittext.getText().toString().trim();
                String encodemessage = Encode.encode(imagename);
                 messageSMS = messageEt.getText().toString().trim();
                pb.setVisibility(View.VISIBLE);

                final  StorageReference reference = storageReference.child(System.currentTimeMillis() + ".jpg" );

                UploadTask uploadTask = reference.putFile(imageUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw  task.getException();
                        }
                        return  reference.getDownloadUrl();
                    }
                })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri downloadUri = task.getResult();

                                    final  String delete = String.valueOf(System.currentTimeMillis());

                                    modal.setMessage(encodemessage);
                                    modal.setSearch(encodemessage.toLowerCase());
                                    modal.setRuid(ruid);
                                    modal.setSuid(suid);
                                    modal.setTime(savetime);
                                    modal.setType("img");
                                    modal.setUrl(downloadUri.toString());

                                    String key = sRef.push().getKey();
                                    modal.setDelete(key);

                                    sRef.child(key).setValue(modal);

                                    rmodal.setMessage(encodemessage);
                                    rmodal.setSearch(encodemessage.toLowerCase());
                                    rmodal.setRuid(ruid);
                                    rmodal.setSuid(suid);
                                    rmodal.setType("img");
                                    rmodal.setUrl(downloadUri.toString());
                                    rmodal.setTime(savetime);

                                    String key2 = rRef.push().getKey();
                                    rmodal.setDelete(key2);

                                    rRef.child(key2).setValue(rmodal);

                                    // chat list ref

                                    listModel.setLastm(encodemessage);
                                    listModel.setName(rname);
                                    listModel.setUid(ruid);
                                    listModel.setUrl(rurl);
                                    listModel.setTime(savetime);

                                    schatlist.child(ruid).setValue(listModel);


                                    // adding data in receiver

                                    rlistmodel.setLastm(encodemessage);
                                    rlistmodel.setName(sname);
                                    rlistmodel.setUrl(surl);
                                    rlistmodel.setUid(suid);
                                    rlistmodel.setTime(savetime);

                                    rchatlist.child(suid).setValue(rlistmodel);

                                    seenStatus.child(suid+ruid).child("counts").child(filename).setValue("sent");

                                }

                                sendNotification(suid, ruid);

                                alertDialog.dismiss();
                                pb.setVisibility(View.GONE);
                            }
                        });
            }
        });

        alertDialog.show();
    }
    public void sendNotification(String suid, String ruid){

        FirebaseDatabase.getInstance().getReference().child("Tokens").child(ruid).child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        usertoken=dataSnapshot.getValue(String.class);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                FcmNotificationsSender notificationsSender =
                        new FcmNotificationsSender(usertoken, "Chat it", sname+  " : " +messageSMS,
                                getApplicationContext(),MessageAct.this);

                notificationsSender.SendNotifications();

                messageEt.setText("");

            }
        },1000);

    }

    @Override
    protected void onStart()
    {
        super.onStart();


        try {
            FirebaseRecyclerOptions<MessageModal> options =
                    new FirebaseRecyclerOptions.Builder<MessageModal>()
                            .setQuery(sRef,MessageModal.class)
                            .build();

            FirebaseRecyclerAdapter<MessageModal,MessageVH> firebaseRecyclerAdapter =
                    new FirebaseRecyclerAdapter<MessageModal, MessageVH>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull MessageVH holder,  int position, @NonNull
                                MessageModal model) {

                            holder.setmessage(getApplication(),model.getMessage(),model.getSearch(),model.getDelete(),model.getTime()
                                    ,model.getSuid(),model.getRuid(),model.getUrl(),
                                    model.getType(),model.getMno(),model.getReaction(), model.getReactionrec());

                            String deletem = getItem(position).getDelete();
                            String message = getItem(position).getMessage();
                            String suid = getItem(position).getSuid();
                            String url = getItem(position).getUrl();
                            String key = getRef(position).getKey();
                            String postkey = getRef(position).getKey();

                            int reactions[] = new int[]{
                                    R.drawable.ic_like, // 0
                                    R.drawable.newlove, //1
                                    R.drawable.ic_laugh, //2
                                    R.drawable.ic_cry,  // 3
                                    R.drawable.ic_sad,  //4
                                    R.drawable.ic_angry  //5
                            };

                            ReactionsConfig config = new ReactionsConfigBuilder(MessageAct.this)
                                    .withReactions(reactions)
                                    .build();

                            ReactionPopup popup = new ReactionPopup(getApplicationContext(),config,(pos) ->{

                                try {
                                    if (rcCode.equals("1")){

                                        Map<String,Object> map = new HashMap<>();
                                        map.put("reaction",pos);

                                        Query query = sRef.orderByChild("delete").equalTo(deletem);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                    dataSnapshot.getRef().updateChildren(map)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        Query query2 = rRef.orderByChild("delete").equalTo(deletem);
                                        query2.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                    dataSnapshot.getRef().updateChildren(map)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                   // Toast.makeText(UpdatePhoto.this, "done", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }else if (rcCode.equals("2")){

                                        Map<String,Object> map = new HashMap<>();
                                        map.put("reactionrec",pos);

                                        Query query = sRef.orderByChild("delete").equalTo(deletem);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                    dataSnapshot.getRef().updateChildren(map)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        Query query2 = rRef.orderByChild("delete").equalTo(deletem);
                                        query2.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                    dataSnapshot.getRef().updateChildren(map)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    // Toast.makeText(UpdatePhoto.this, "done", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }catch (Exception e){
                                    Toast.makeText(MessageAct.this, "error"+e, Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            });


                            holder.mTvs.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    popup.onTouch(view,motionEvent);
                                    rcCode = "1";
                                    return false;
                                }
                            });

                            holder.mtvr.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    popup.onTouch(view,motionEvent);
                                    rcCode = "2";
                                    return false;
                                }
                            });

                            holder.mTvs.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    messageSheet(deletem,message,suid);

                                }
                            });

                            holder.mtvr.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    messageSheet(deletem,message,suid);

                                }
                            });

                            holder.share_sphoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    forwardimage(url,sname,suid,delete,message);
                                }

                            });

                            holder.share_rphoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    forwardimage(url,sname,suid,delete,message);


                                }
                            });

                        }

                        @NonNull
                        @Override
                        public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.message_item,parent,false);

                            return new MessageVH(view);

                        }

                    };

            recyclerViewm.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
            scrolldownOnnewMessage(firebaseRecyclerAdapter);


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerViewm.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerViewm.scrollToPosition(firebaseRecyclerAdapter.getItemCount() - 1);
                            // Here adapter.getItemCount()== child count
                        }
                    });
                }
            },1000);

        }catch (Exception e){

            Toast.makeText(this, "Server Lost", Toast.LENGTH_SHORT).show();
        }

    }

    private void scrolldownOnnewMessage(FirebaseRecyclerAdapter<MessageModal, MessageVH> firebaseRecyclerAdapter) {

        sRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                recyclerViewm.post(new Runnable() {
                    @Override
                    public void run() {

                        recyclerViewm.scrollToPosition(firebaseRecyclerAdapter.getItemCount() - 1);
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void forwardimage(String picurl, String sname, String suid, String delete, String message) {

        final Dialog dialog = new Dialog(MessageAct.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forward_bs);

        lastmREf = database.getReference("lastm");

        RecyclerView recyclerView = dialog.findViewById(R.id.rv_forward);
        Spinner spinner = dialog.findViewById(R.id.spinnerf);
        String[] items = {"Chats","Groups"};

        LinearLayoutManager manager = new LinearLayoutManager(MessageAct.this);

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);


        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        // textView.setText("Chats");
                        showchats();
                        break;
                    case 1:
                        //  textView.setText("Groups");
                        showgroups();
                        break;
                }
            }

            private void showchats() {

                schatlist = database.getReference("chat list").child(suid);

                FirebaseRecyclerOptions<ListModal> options =
                        new FirebaseRecyclerOptions.Builder<ListModal>()
                                .setQuery(schatlist,ListModal.class)
                                .build();

                FirebaseRecyclerAdapter<ListModal,ListVH> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<ListModal, ListVH>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull ListVH holder, int position,
                                                            @NonNull ListModal model) {

                                holder.setListforward(getApplication(),model.getTime(),
                                        model.getLastm(),model.getName()
                                        ,model.getUrl(),model.getUid());


                                String ruid = getItem(position).getUid();

                                holder.seentv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public  void onClick(View view) {

                                        final  String delete = String.valueOf(System.currentTimeMillis());

                                        modal.setMessage(message);
                                        modal.setSearch(message.toLowerCase());
                                        modal.setRuid(ruid);
                                        modal.setSuid(suid);
                                        modal.setTime(savetime);
                                        modal.setType("img");
                                        modal.setUrl(picurl);

                                        String key = sRef.push().getKey();
                                        modal.setDelete(key);

                                        sRef.child(key).setValue(modal);

                                        rmodal.setMessage(message);
                                        rmodal.setSearch(message);
                                        rmodal.setRuid(ruid);
                                        rmodal.setSuid(suid);
                                        rmodal.setType("img");
                                        rmodal.setUrl(picurl);
                                        rmodal.setTime(savetime);

                                        String key2 = rRef.push().getKey();
                                        rmodal.setDelete(key2);

                                        rRef.child(key2).setValue(rmodal);

                                        // chat list ref

                                        listModel.setLastm(message);
                                        listModel.setName(rname);
                                        listModel.setUid(ruid);
                                        listModel.setUrl(rurl);
                                        listModel.setTime(savetime);

                                        schatlist.child(ruid).setValue(listModel);

                                        // adding data in receiver

                                        rlistmodel.setLastm(message);
                                        rlistmodel.setName(sname);
                                        rlistmodel.setUid(suid);
                                        rlistmodel.setUrl(surl);
                                        rlistmodel.setTime(savetime);

                                        rchatlist.child(suid).setValue(rlistmodel);

                                        seenStatus.child(suid+ruid).child("counts").child(Decode.decode(message)).setValue("sent");

                                    }
                                });
                            }
                            @NonNull
                            @Override
                            public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.liste_itemf,parent,false);

                                return new ListVH(view);
                            }
                        };

                recyclerView.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();
            }

            private void showgroups() {
                groupRef = database.getReference("groups").child(suid);
                GroupChatModal messageModal = new GroupChatModal();

                FirebaseRecyclerOptions<GroupModal> options =
                        new FirebaseRecyclerOptions.Builder<GroupModal>()
                                .setQuery(groupRef,GroupModal.class)
                                .build();

                FirebaseRecyclerAdapter<GroupModal,GroupVH> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<GroupModal, GroupVH>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull GroupVH holder, int position, @NonNull GroupModal model) {

                                holder.setGroupforward(getApplication(),model.getAdmin(),model.getAdminid(),model.getUrl(),model.getAddress()
                                        ,model.getSearch(),model.getGroupname(),model.getTime(),model.getDelete(),model.getLastm(),model.getLastmtime());

                                String address = getItem(position).getAddress();
                                String url = getItem(position).getUrl();
                                String groupname = getItem(position).getGroupname();
                                String adminid = getItem(position).getAdmin();
                                groupchat = database.getReference("group chat").child(address);


                                holder.timetv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        messageModal.setMessage(message);
                                        messageModal.setDelete(String.valueOf(System.currentTimeMillis()));
                                        messageModal.setTime(savetime);
                                        messageModal.setSuid(suid);
                                        messageModal.setSearch(message.toLowerCase());
                                        messageModal.setSeen("no");
                                        messageModal.setSname(sname);
                                        messageModal.setType("img");
                                        messageModal.setUrl(picurl);

                                        String key = groupchat.push().getKey();
                                        groupchat.child(key).setValue(messageModal);

                                        lastmModal.setLastm(message);
                                        lastmModal.setLastmtime(savetime);

                                        lastmREf.child(address).setValue(lastmModal);

                                        Map<String,Object> map = new HashMap<>();
                                        map.put("lastm",message);
                                        map.put("lastmtime",savetime);

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("groups").child(suid).child(address)
                                                .updateChildren(map)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.timetv.setText("Sent");
                                            }
                                        },2000);
                                    }
                                });
                            }

                            @NonNull
                            @Override
                            public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.group_item_f,parent,false);

                                return new GroupVH(view);

                            }
                        };

                recyclerView.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();

            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void savelocally( String delete,Uri url, String message){
        ProgressDialog pd = new ProgressDialog(MessageAct.this);
        pd.setTitle("Loading");
        pd.setMessage("Please wait");
        pd.show();

        String path = "chat it/" + Decode.decode(message) + System.currentTimeMillis()+".jpg";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI  | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Chat It");
        request.setDescription("Downlaoding Image");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,path);
        DownloadManager manager = (DownloadManager) MessageAct.this.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        Toast.makeText(this, "Downloading", Toast.LENGTH_SHORT).show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Map<String,Object> map = new HashMap<>();
                map.put("url","/storage/emulated/0/Download/"+path);

                Query query = sRef.orderByChild("delete").equalTo(delete);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            dataSnapshot.getRef().updateChildren(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(MessageAct.this, "Image saved", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                pd.dismiss();
            }
        },2000);


    }

    private void getdata() {
        try {
            lastseenref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        String userstatus = snapshot.child(ruid).getValue().toString();
                        lasteentv.setText(userstatus);
                    }else {
                        lasteentv.setText("Long time ago");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }catch (Exception e){

        }
    }

    private  void onlineuser(){

        lastseenref.child(suid).setValue("online");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        lastseenref.child(suid).setValue("Last seen "+savetime);
        typingstatus = "0";
    }

    @Override
    protected void onStop() {
        super.onStop();
        lastseenref.child(suid).setValue("Last seen "+savetime);

    }
    // bottomsheet for message option
    private void messageSheet(String delete, String message, String suid2) {

        final Dialog dialog = new Dialog(MessageAct.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.group_bs);

        TextView deletetv = dialog.findViewById(R.id.del_gc_m);
        TextView forwardtv = dialog.findViewById(R.id.forward_gc_m);
        TextView copytv = dialog.findViewById(R.id.copy_gc_m);
        TextView replyp = dialog.findViewById(R.id.replyp_gc_m);
        replyp.setVisibility(View.GONE);

        if (suid2.equals(suid)){
            deletetv.setVisibility(View.VISIBLE);
        }else {
            deletetv.setVisibility(View.GONE);
        }

        deletetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sRef.child(delete).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Toast.makeText(MessageAct.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        copytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboardManager = (ClipboardManager) MessageAct.this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("String",message);
                clipboardManager.setPrimaryClip(clip);
                clip.getDescription();

                Toast.makeText(MessageAct.this, "copied", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        forwardtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    forwardMessage(message);

                }catch (Exception e){

                    Toast.makeText(MessageAct.this, ""+e, Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void forwardMessage(String message) {

        final Dialog dialog = new Dialog(MessageAct.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forward_bs);

        RecyclerView recyclerView = dialog.findViewById(R.id.rv_forward);
        Spinner spinner = dialog.findViewById(R.id.spinnerf);
        String[] items = {"Chats","Groups"};

        LinearLayoutManager manager = new LinearLayoutManager(MessageAct.this);

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);


        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        // textView.setText("Chats");
                        showchats();
                        break;
                    case 1:
                        //  textView.setText("Groups");
                        showgroups();
                        break;
                }
            }

            private void showchats() {

                schatlist = database.getReference("chat list").child(suid);

                FirebaseRecyclerOptions<ListModal> options =
                        new FirebaseRecyclerOptions.Builder<ListModal>()
                                .setQuery(schatlist,ListModal.class)
                                .build();

                FirebaseRecyclerAdapter<ListModal,ListVH> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<ListModal, ListVH>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull ListVH holder, int position,
                                                            @NonNull ListModal model) {

                                holder.setListforward(getApplication(),model.getTime(),model.getLastm(),model.getName()
                                        ,model.getUrl(),model.getUid());


                                String ruid = getItem(position).getUid();

                                holder.seentv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public  void onClick(View view) {

                                        sRef = database.getReference("Message").child(suid).child(ruid);
                                        rRef = database.getReference("Message").child(ruid).child(suid);
                                        rchatlist = database.getReference("chat list").child(ruid);
                                        holder.seentv.setText("....");

                                        //   String encodemessage = Encode.encode(message);

                                        // set the code to the edit text

                                        modal.setMessage(message);
                                        modal.setSearch(message.toLowerCase());
                                        modal.setRuid(ruid);
                                        modal.setSuid(suid);
                                        modal.setTime(savetime);
                                        modal.setDelete(String.valueOf(System.currentTimeMillis()));

                                        String key = sRef.push().getKey();

                                        sRef.child(key).setValue(modal);
                                        messageEt.setText("");

                                        rmodal.setMessage(message);
                                        rmodal.setSearch(message.toLowerCase());
                                        rmodal.setRuid(ruid);
                                        rmodal.setSuid(suid);
                                        rmodal.setTime(savetime);
                                        rmodal.setDelete(String.valueOf(System.currentTimeMillis()));

                                        seenStatus.child(suid+ruid).child("counts").child(Decode.decode(message)).setValue("sent");

                                        String key2 = rRef.push().getKey();

                                        rRef.child(key2).setValue(rmodal);
                                        messageEt.setText("");

                                        // chat list ref

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                listModel.setLastm(message);
                                                listModel.setName(rname);
                                                listModel.setUid(ruid);
                                                listModel.setUrl(rurl);
                                                listModel.setTime(savetime);

                                                schatlist.child(ruid).setValue(listModel);

                                                // adding data in receiver

                                                rlistmodel.setLastm(message);
                                                rlistmodel.setName(sname);
                                                rlistmodel.setUid(suid);
                                                rlistmodel.setUrl(surl);
                                                rlistmodel.setTime(savetime);

                                                rchatlist.child(suid).setValue(rlistmodel);
                                                holder.seentv.setText("Sent");
                                            }
                                        },1000);

                                    }
                                });
                            }

                            @NonNull
                            @Override
                            public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.liste_itemf,parent,false);

                                return new ListVH(view);
                            }
                        };

                recyclerView.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();
            }

            private void showgroups() {
                groupRef = database.getReference("groups").child(suid);
                GroupChatModal messageModal = new GroupChatModal();

                FirebaseRecyclerOptions<GroupModal> options =
                        new FirebaseRecyclerOptions.Builder<GroupModal>()
                                .setQuery(groupRef,GroupModal.class)
                                .build();

                FirebaseRecyclerAdapter<GroupModal,GroupVH> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<GroupModal, GroupVH>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull GroupVH holder, int position, @NonNull GroupModal model) {

                                holder.setGroupforward(getApplication(),model.getAdmin(),model.getAdminid(),model.getUrl(),model.getAddress()
                                        ,model.getSearch(),model.getGroupname(),model.getTime(),model.getDelete(),model.getLastm(),model.getLastmtime());

                                String address = getItem(position).getAddress();
                                String url = getItem(position).getUrl();
                                String groupname = getItem(position).getGroupname();
                                String adminid = getItem(position).getAdmin();
                                groupchat = database.getReference("group chat").child(address);


                                holder.timetv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        holder.timetv.setText("....");
                                        messageModal.setMessage(message);
                                        messageModal.setDelete(String.valueOf(System.currentTimeMillis()));
                                        messageModal.setTime(savetime);
                                        messageModal.setSuid(suid);
                                        messageModal.setSearch(message.toLowerCase());
                                        messageModal.setSeen("no");
                                        messageModal.setSname(sname);

                                        String key = groupchat.push().getKey();
                                        groupchat.child(key).setValue(messageModal);


                                        Map<String,Object> map = new HashMap<>();
                                        map.put("lastm",message);
                                        map.put("lastmtime",savetime);

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("groups").child(suid).child(address)
                                                .updateChildren(map)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.timetv.setText("Sent");
                                            }
                                        },2000);
                                    }
                                });
                            }

                            @NonNull
                            @Override
                            public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.group_item_f,parent,false);

                                return new GroupVH(view);

                            }
                        };

                recyclerView.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();

            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomAnim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

//    private void updateViewed(){
//
//        Query query = sRef.orderByChild("suid").equalTo(suid);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    dataSnapshot.getRef().updateChildren(mno)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        Query query1= rRef.orderByChild("ruid").equalTo(ruid);
//        query1.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    dataSnapshot.getRef().updateChildren(mno)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//
//                                }
//                            });
//                }}
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }

}