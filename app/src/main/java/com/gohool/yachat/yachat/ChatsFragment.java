package com.gohool.yachat.yachat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options = new FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(conversationQuery, Conv.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_single_layout, viewGroup, false);

                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder convViewholder, int position, @NonNull final Conv conv) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        if (dataSnapshot.hasChild("message")) {

                            String data = dataSnapshot.child("message").getValue().toString();
                            convViewholder.setMessage(data, conv.isSeen());
                        }


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewholder.setUserOnline(userOnline);
                        }

                        convViewholder.setName(userName);
                        convViewholder.setUserImage(userThumb);

                        convViewholder.mView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view){

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });


        }
    };

     adapter.startListening();
     mConvList.setAdapter(adapter);

    }


    public static class ConvViewHolder extends RecyclerView.ViewHolder {

            View mView;

            public ConvViewHolder(View itemView) {
                super(itemView);

                mView = itemView;
            }


            public void setMessage(String message, boolean isSeen) {

                TextView userStatusView = mView.findViewById(R.id.user_single_status);
                userStatusView.setText(message);

                if(!isSeen) {

                    userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);

                } else {

                    userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                }

            }


            public void setName(String name) {

                TextView userNameView = mView.findViewById(R.id.user_single_name);
                userNameView.setText(name);
            }


            public void setUserImage(String thumb_image) {

                CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
                Picasso.get().load(thumb_image).placeholder(R.drawable.default_user).into(userImageView);
            }


            public void setUserOnline(String online_status) {

                ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);

                if(online_status.equals("true")) {

                    userOnlineView.setVisibility(View.VISIBLE);

                } else {

                    userOnlineView.setVisibility(View.INVISIBLE);
                }
            }


        }
}



