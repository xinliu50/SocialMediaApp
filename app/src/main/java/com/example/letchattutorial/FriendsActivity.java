package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UserRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        InitialUI();

    }

    private void InitialUI() {
        myFriendList = (RecyclerView)findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
        DisplayAllFriends();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>().setQuery( FriendsRef,Friends.class).build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());
                final String usersIDs = getRef(position).getKey();

                UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileImage").getValue().toString();

                            holder.setFullname(userName);
                            holder.setProfileImage(profileImage);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{
                                      userName + "'s Profile",
                                      "Send Message"
                                    };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("visit_user_id",usersIDs);
                                                startActivity(profileIntent);
                                            }
                                            if(which == 1){
                                                Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("visit_user_id",usersIDs);
                                                chatIntent.putExtra("userName",userName);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                return new FriendsViewHolder(view);
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setProfileImage(String profileImage) {
            final CircleImageView myImage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            StorageReference path = FirebaseStorage.getInstance().getReference(profileImage);
            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).placeholder(R.drawable.profile).into(myImage);
                }
            });
        }

        public void setFullname(String fullname) {
            TextView myName = (TextView)mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }

        public void setDate(String date) {
            TextView friendsDate = (TextView)mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends Since: "+date);
        }
    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        HashMap currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UserRef.child(online_user_id).child("userState")
                .updateChildren(currentStateMap);
    }
}
