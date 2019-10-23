package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;
    private String Post_Key, current_user_id;
    private DatabaseReference UserRef, PostRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        InitialUI();

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(userName);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void InitialUI(){
        PostCommentButton = (ImageButton)findViewById(R.id.post_comment_button);
        Post_Key = getIntent().getExtras().get("PostKey").toString();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        PostRef = FirebaseDatabase.getInstance().getReference("Posts").child(Post_Key).child("Comments");

        CommentsList = (RecyclerView)findViewById(R.id.comment_list);
        CommentInputText = (EditText)findViewById(R.id.comment_input);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options=new FirebaseRecyclerOptions.Builder<Comments>().setQuery( PostRef,Comments.class).build();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.setUsername(model.getUsername());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setComment(model.getComment());
            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent,false);
                return new CommentsViewHolder(view);
            }
        };
        CommentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setUsername(String username) {
            TextView myUserName = (TextView)mView.findViewById(R.id.comment_username);
            myUserName.setText("@" +username+"  ");
        }

        public void setTime(String time) {
            TextView myTime = (TextView)mView.findViewById(R.id.comment_time);
            myTime.setText("  Time: "+time);
        }

        public void setDate(String date) {
            TextView myDate = (TextView)mView.findViewById(R.id.comment_date);
            myDate.setText("Date: "+date);
        }

        public void setComment(String comment) {
            TextView myComment = (TextView)mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
    }


    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this,"Please write some comments",Toast.LENGTH_LONG).show();
        }else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);

            PostRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this,"You have commented successfully",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(CommentsActivity.this, "Error Occurred, Try again..",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
