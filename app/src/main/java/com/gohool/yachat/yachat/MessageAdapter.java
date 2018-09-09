package com.gohool.yachat.yachat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

     private List<Messages> mMessageList;
     private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;


     public MessageAdapter(List<Messages> mMessageList) {

         this.mMessageList = mMessageList;
     }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

         View v = LayoutInflater.from(viewGroup.getContext())
                 .inflate(R.layout.message_single_layout, viewGroup, false);

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            messageImage = view.findViewById(R.id.message_image_layout);
        }

        public void setName(String name) {

            displayName.setText(name);
        }

        public void setUserImage(String thumb_image) {

            Picasso.get().load(thumb_image).placeholder(R.drawable.default_user).into(profileImage);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

         mAuth = FirebaseAuth.getInstance();

         final Messages c = mMessageList.get(i);

         final String from_user = c.getFrom();

         String message_type = c.getType();

         mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

         final String current_user_id = mAuth.getCurrentUser().getUid();


//         if(from_user.equals(current_user_id)) {
//
//                    messageViewHolder.messageText.setBackgroundColor(Color.WHITE);
//                    messageViewHolder.messageText.setTextColor(Color.BLACK);
//
//
//         } else {
//
//
//                    messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
//                    messageViewHolder.messageText.setTextColor(Color.WHITE);
//
//         }
//
//
//        messageViewHolder.messageText.setText(c.getMessage());




        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                messageViewHolder.setName(name);
                messageViewHolder.setUserImage(userThumb);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            messageViewHolder.messageText.setText(c.getMessage());
            messageViewHolder.messageImage.setVisibility(View.INVISIBLE);

        } else {

            messageViewHolder.messageText.setVisibility(View.INVISIBLE);

            Picasso.get().load(c.getMessage()).placeholder(R.drawable.common_google_signin_btn_icon_light_normal_background).into(messageViewHolder.messageImage);

        }





    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


}
