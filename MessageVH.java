package com.example.chatit;

import android.app.Application;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.logging.Handler;

public class MessageVH extends RecyclerView.ViewHolder {

    String currentuid;
    TextView mTvs,mtvr,filenames,filenamer,timetvs,timetvr;
    ImageButton downlaodr,expandr,sdownload;
    ConstraintLayout cls,clr;

    ImageView iv_rphoto,iv_sphoto,iv_reactions_s,iv_reactions_s_2,iv_reactions_r,iv_reactions_r_2
            ,share_rphoto,share_sphoto,ticksenderiv;


    public MessageVH(@NonNull View itemView) {
        super(itemView);
    }

    public void setmessage(Application application, String message, String search, String delete, String time,
                           String suid, String ruid,String url,String type,
                           int reaction, int reactionrec,String read,String delivered){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid  = user.getUid();

        mtvr = itemView.findViewById(R.id.m_tv_r);
        mTvs = itemView.findViewById(R.id.m_tv_s);


        iv_reactions_r = itemView.findViewById(R.id.reaction_iv_r);
        iv_reactions_s = itemView.findViewById(R.id.reaction_iv_s);
        iv_reactions_r_2 = itemView.findViewById(R.id.reaction_iv_r_2);
        iv_reactions_s_2 = itemView.findViewById(R.id.reaction_iv_s_2);

        iv_rphoto = itemView.findViewById(R.id.iv_r);
        iv_sphoto = itemView.findViewById(R.id.iv_s);
        share_rphoto = itemView.findViewById(R.id.sharephoto_r);
        share_sphoto = itemView.findViewById(R.id.sharephoto_s);

        timetvr = itemView.findViewById(R.id.time_r_photo);
        timetvs = itemView.findViewById(R.id.time_s_photo);

        filenamer = itemView.findViewById(R.id.m_tvr_photo);
        filenames = itemView.findViewById(R.id.m_tvs_photo);

        clr = itemView.findViewById(R.id.cl_iv_r);
        cls = itemView.findViewById(R.id.cl_iv_s);

        ticksenderiv = itemView.findViewById(R.id.tickivsender);

        String decodemessage = Decode.decode(message);

        try {
            if (delivered.equals("yes")){
                if (read.equals("yes")){
                    ticksenderiv.setImageResource(R.drawable.baseline_check_circle_24);
                }else if (read.equals("no")){
                    ticksenderiv.setImageResource(R.drawable.baseline_check_circle_outline_24);
                }
            }else {
                ticksenderiv.setImageResource(R.drawable.baseline_check_circle_grey);

            }
        }catch (Exception e){

        }

        if (currentuid.equals(suid)){

            if (type.equals("img")){
                mtvr.setVisibility(View.GONE);
                clr.setVisibility(View.GONE);
                filenames.setText(decodemessage);
                cls.setVisibility(View.VISIBLE);
                mTvs.setVisibility(View.GONE);
                Picasso.get().load(url).into(iv_sphoto);
                timetvs.setText(time);

            }else if (type.equals("txt")){
                   // sender side
                cls.setVisibility(View.GONE);
                clr.setVisibility(View.GONE);
                mtvr.setVisibility(View.GONE);
                mTvs.setVisibility(View.VISIBLE);
                mTvs.setText(decodemessage);


                switch (reaction) {
                    case 0:
                        iv_reactions_s.setImageResource(R.drawable.ic_like);
                        iv_reactions_s.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        iv_reactions_s.setImageResource(R.drawable.newlove);
                        iv_reactions_s.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        iv_reactions_s.setImageResource(R.drawable.ic_laugh);
                        iv_reactions_s.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        iv_reactions_s.setImageResource(R.drawable.ic_cry);
                        iv_reactions_s.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        iv_reactions_s.setImageResource(R.drawable.ic_sad);
                        iv_reactions_s.setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        iv_reactions_s.setImageResource(R.drawable.ic_angry);
                        iv_reactions_s.setVisibility(View.VISIBLE);
                        break;

                }

                switch (reactionrec){
                        case  0:
                            iv_reactions_s_2.setImageResource(R.drawable.ic_like);
                            iv_reactions_s_2.setVisibility(View.VISIBLE);
                            break;
                        case  1:
                            iv_reactions_s_2.setImageResource(R.drawable.newlove);
                        iv_reactions_s_2.setVisibility(View.VISIBLE);
                        break;
                    case  2:
                        iv_reactions_s_2.setImageResource(R.drawable.ic_laugh);
                        iv_reactions_s_2.setVisibility(View.VISIBLE);
                        break;
                    case  3:
                        iv_reactions_s_2.setImageResource(R.drawable.ic_cry);
                        iv_reactions_s_2.setVisibility(View.VISIBLE);
                        break;
                    case  4:
                        iv_reactions_s_2.setImageResource(R.drawable.ic_sad);
                        iv_reactions_s_2.setVisibility(View.VISIBLE);
                        break;
                    case  5:
                        iv_reactions_s_2.setImageResource(R.drawable.ic_angry);
                        iv_reactions_s_2.setVisibility(View.VISIBLE);
                        break;
                }

            }

        }else if (currentuid.equals(ruid)){

            ticksenderiv.setVisibility(View.GONE);
            if (type.equals("img")){

                mTvs.setVisibility(View.GONE);
                cls.setVisibility(View.GONE);
                filenamer.setText(decodemessage );
                clr.setVisibility(View.VISIBLE);
                mtvr.setVisibility(View.GONE);
                Picasso.get().load(url).into(iv_rphoto);
                timetvr.setText(time);

            }else if (type.equals("txt")){

                cls.setVisibility(View.GONE);
                clr.setVisibility(View.GONE);
                mTvs.setVisibility(View.GONE);
                mtvr.setVisibility(View.VISIBLE);
                mtvr.setText(decodemessage);

                // receiver side
                switch (reaction){
                    case  0:
                        iv_reactions_r.setImageResource(R.drawable.ic_like);
                        iv_reactions_r.setVisibility(View.VISIBLE);
                        break;
                    case  1:
                        iv_reactions_r.setImageResource(R.drawable.newlove);
                        iv_reactions_r.setVisibility(View.VISIBLE);
                        break;
                    case  2:
                        iv_reactions_r.setImageResource(R.drawable.ic_laugh);
                        iv_reactions_r.setVisibility(View.VISIBLE);
                        break;
                    case  3:
                        iv_reactions_r.setImageResource(R.drawable.ic_cry);
                        iv_reactions_r.setVisibility(View.VISIBLE);
                        break;
                    case  4:
                        iv_reactions_r.setImageResource(R.drawable.ic_sad);
                        iv_reactions_r.setVisibility(View.VISIBLE);
                        break;
                    case  5:
                        iv_reactions_r.setImageResource(R.drawable.ic_angry);
                        iv_reactions_r.setVisibility(View.VISIBLE);
                        break;
                }

                switch (reactionrec){
                    case  0:
                        iv_reactions_r_2.setImageResource(R.drawable.ic_like);
                        iv_reactions_r_2.setVisibility(View.VISIBLE);
                        break;
                    case  1:
                        iv_reactions_r_2.setImageResource(R.drawable.newlove);
                        iv_reactions_r_2.setVisibility(View.VISIBLE);
                        break;
                    case  2:
                        iv_reactions_r_2.setImageResource(R.drawable.ic_laugh);
                        iv_reactions_r_2.setVisibility(View.VISIBLE);
                        break;
                    case  3:
                        iv_reactions_r_2.setImageResource(R.drawable.ic_cry);
                        iv_reactions_r_2.setVisibility(View.VISIBLE);
                        break;
                    case  4:
                        iv_reactions_r_2.setImageResource(R.drawable.ic_sad);
                        iv_reactions_r_2.setVisibility(View.VISIBLE);
                        break;
                    case  5:
                        iv_reactions_r_2.setImageResource(R.drawable.ic_angry);
                        iv_reactions_r_2.setVisibility(View.VISIBLE);
                        break;
                }

            }}}

        public void downloadstatus(String key){

        DatabaseReference download = FirebaseDatabase.getInstance().getReference("download");
        download.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(key)){
                    downlaodr.setVisibility(View.GONE);
                    expandr.setVisibility(View.VISIBLE);
                }else {
                    expandr.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }


}

