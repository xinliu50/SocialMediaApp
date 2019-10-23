package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference UserRef, PostRef, LikesRef;
    private StorageReference UserStorageRef;
    private View navView;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private String CurrentUserId;
    private Uri image;
    final String TAG = "status";
    private ImageButton AddNewPostButton;
    private FirebaseRecyclerOptions<Posts> options;
    private  FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter;
    private GoogleSignInClient mGoogleSignInClient;
    private Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitialUI();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileImage")){
                        String path = dataSnapshot.child("profileImage").getValue().toString();
                        StorageReference filePath = FirebaseStorage.getInstance().getReference(path);
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).placeholder(R.drawable.profile).into(NavProfileImage);
                            }
                        });
                    }

                    else{
                        Toast.makeText(MainActivity.this,"Profile name do not exists",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });
       // DisplayAllUsersPosts();
    }

    private void InitialUI(){
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        UserRef = database.getReference().child("Users");
        PostRef = database.getReference().child("Posts");
        CurrentUserId = mAuth.getCurrentUser().getUid();
        AddNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView)navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView)navView.findViewById(R.id.nav_user_full_name);
        postList = (RecyclerView)findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

    }

    private void DisplayAllUsersPosts() {
        Query SortPostsInDecendingOrder = PostRef.orderByChild("counter");
        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery( SortPostsInDecendingOrder,Posts.class).build();
        FirebaseRecyclerAdapter firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
               // PostsViewHolder viewHolder=new PostsViewHolder(view);
                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull Posts model) {
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
                        Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
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
                                    Log.d("status:CurrentUserId: ",CurrentUserId);
                                    if(dataSnapshot.child(PostKey).hasChild(CurrentUserId)){
                                        LikesRef.child(PostKey).child(CurrentUserId).removeValue();
                                        LikeChecker = false;
                                    }else{
                                        LikesRef.child(PostKey).child(CurrentUserId).setValue(true);
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

                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MainActivity.this,CommentsActivity.class);
                        commentIntent.putExtra("PostKey",PostKey);
                        startActivity(commentIntent);
                    }
                });

            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        //firebaseRecyclerAdapter.startListening();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        TextView username,date,time,description,DisplayNoOfLikes;
        CircleImageView user_post_image;
        ImageView postImage;
        View mView;
        ImageButton LikepostButton, CommentPostButton;
        int countLikes;
        String currentUserId;
        DatabaseReference LikeRef;


        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

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

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currenUser = mAuth.getCurrentUser();
        DisplayAllUsersPosts();

        if(currenUser == null){
            SendUserToLoginActivity();
        }else{
            CheckUserExistence();
        }
    }


    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetUpActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_message:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                mGoogleSignInClient.signOut()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        Log.d("status","log out succ");
                                }
                            });
                SendUserToLoginActivity();

                break;

        }
    }

    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendActivity.class);
        startActivity(findFriendsIntent);
    }

    private void SendUserToSettingsActivity() {
        Intent settingIntent = new Intent(MainActivity.this,SettingActivity.class);
        startActivity(settingIntent);
    }

    private void SendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileIntent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
