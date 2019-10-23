package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;
    private RecyclerView SearchResultList;
    private FirebaseRecyclerOptions<FindFriends> options;
    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        InitialUI();

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = SearchInputText.getText().toString();
                SearchPeopleAndFriends(searchBoxInput);
            }
        });
    }

    private void InitialUI(){
        mToolbar = (Toolbar)findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Friends");

        SearchResultList = (RecyclerView)findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton)findViewById(R.id.search_people_friends_button);
        SearchInputText = (EditText)findViewById(R.id.search_box_input);
        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    private void SearchPeopleAndFriends(String searchBoxInput) {
        Toast.makeText(this,"Searching....", Toast.LENGTH_LONG).show();
        Query SearchPeopleandFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery( SearchPeopleandFriendsQuery,FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends, FindFriendActivity.FindFriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<FindFriends, FindFriendActivity.FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {
                //final String PostKey = getRef(position).getKey();
                holder.setFullname(model.getFullname());
                holder.setStatus(model.getStatus());
                holder.setProfileImage(model.getProfileImage());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendActivity.FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                FindFriendActivity.FindFriendsViewHolder viewHolder=new FindFriendActivity.FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView myImage;
        TextView myName, myStatus;
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setProfileImage(String profileImage) {
            myImage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            StorageReference path = FirebaseStorage.getInstance().getReference(profileImage);
            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).placeholder(R.drawable.profile).into(myImage);
                }
            });
        }

        public void setFullname(String fullname) {
            myName = (TextView)mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }

        public void setStatus(String status) {
            myStatus = (TextView)mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }
    }

}
