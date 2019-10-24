package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, UserRef, LikesRef;
    private String currentUserID;
    private Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        InitialUI();
    }

    private void InitialUI() {
        mToolbar = (Toolbar)findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = (RecyclerView)findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts() {
        Query myPostsQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery( myPostsQuery,Posts.class).build();
        FirebaseRecyclerAdapter<Posts, MyPostesViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, MyPostesViewHolder>(options) {

            @NonNull
            @Override
            public  MyPostesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
                return new  MyPostesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyPostesViewHolder holder, int position, @NonNull Posts model) {
                final String PostKey = getRef(position).getKey();

                holder.setFullname(model.getFullname());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileimage(model.getProfileimage());
                holder.setPostimage(model.getPostimage());

                holder.setLikeButtonStatus(PostKey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickPostIntent);
                    }
                });


                holder.LikepostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true)){
                                    Log.d("status: postKey!!!: ",PostKey);
                                    Log.d("status:CurrentUserId: ",currentUserID);
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }else{
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MyPostsActivity.this,CommentsActivity.class);
                        commentIntent.putExtra("PostKey",PostKey);
                        startActivity(commentIntent);
                    }
                });

                holder.LikepostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true)){
                                    Log.d("status: postKey!!!: ",PostKey);
                                    Log.d("status:CurrentUserId: ",currentUserID);
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }else{
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                        //LikesRef.child(PostKey).push(CurrentUserId).setValue(true);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        myPostsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class MyPostesViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView username,date,time,description,DisplayNoOfLikes;
        CircleImageView user_post_image;
        ImageView postImage;
        ImageButton LikepostButton, CommentPostButton;
        int countLikes;
        String currentUserId;
        DatabaseReference LikeRef;


        public MyPostesViewHolder(@NonNull View itemView) {
            super(itemView);
            LikepostButton = (ImageButton) itemView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) itemView.findViewById(R.id.display_no_of_like);
            LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setTime(String time) {
            this.time=itemView.findViewById(R.id.post_time);
            this.time.setText(time);
        }

        public void setDate(String date) {
            this.date = itemView.findViewById(R.id.post_date);
            this.date.setText(date);
        }

        public void setPostimage(String postimage) {
            postImage = (ImageView) itemView.findViewById(R.id.post_image);
            StorageReference filePath = FirebaseStorage.getInstance().getReference(postimage);
            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(postImage);
                }
            });
        }

        public void setDescription(String description) {
            this.description = itemView.findViewById(R.id.all_post_description);
            this.description.setText(description);
        }

        public void setProfileimage(String profileimage) {
            user_post_image = (CircleImageView)itemView.findViewById(R.id.post_user_image);
            StorageReference filePath = FirebaseStorage.getInstance().getReference(profileimage);
            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(user_post_image);
                }
            });

        }

        public void setFullname(String fullname) {
            this.username = itemView.findViewById(R.id.post_user_name);
            this.username.setText(fullname);
        }

        public void setLikeButtonStatus(final String PostKey){
            LikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikepostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }else{
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikepostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
