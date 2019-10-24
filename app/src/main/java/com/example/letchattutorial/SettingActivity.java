package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProImage;
    private DatabaseReference SettingUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private final static int Gallery_Pick = 1;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        InitialUI();

        SettingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = "";
                    String myUserName = "";
                    String myProfileName = "";
                    String myProfileStatus = "";
                    String myDOB = "";
                    String myCountry = "";
                    String myGender = "";
                    String myRelationStatus = "";

                    if(dataSnapshot.hasChild("profileImage"))
                        myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    if(dataSnapshot.hasChild("username"))
                        myUserName = dataSnapshot.child("username").getValue().toString();
                    if(dataSnapshot.hasChild("fullname"))
                        myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    if(dataSnapshot.hasChild("status"))
                        myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    if(dataSnapshot.hasChild("dob"))
                        myDOB = dataSnapshot.child("dob").getValue().toString();
                    if(dataSnapshot.hasChild("country"))
                        myCountry = dataSnapshot.child("country").getValue().toString();
                    if(dataSnapshot.hasChild("gender"))
                        myGender = dataSnapshot.child("gender").getValue().toString();
                    if(dataSnapshot.hasChild("relationshipstatus"))
                        myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    if(!myProfileImage.equals("")) {
                        StorageReference path = FirebaseStorage.getInstance().getReference(myProfileImage);
                        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).placeholder(R.drawable.profile).into(userProImage);
                            }
                        });
                    }
                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        userProImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }

    private void InitialUI(){
        mToolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText)findViewById(R.id.settings_username);
        userProfName = (EditText)findViewById(R.id.settings_profile_full_name);
        userStatus = (EditText)findViewById(R.id.settings_status);
        userCountry = (EditText)findViewById(R.id.settings_country);
        userGender = (EditText)findViewById(R.id.settings_gender);
        userRelation = (EditText)findViewById(R.id.settings_relationship_status);
        userDOB = (EditText)findViewById(R.id.settings_dob);
        userProImage = (CircleImageView)findViewById(R.id.settings_profile_image);
        UpdateAccountSettingsButton = (Button)findViewById(R.id.update_account_settings_buttons);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        loadingBar = new ProgressDialog(this);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait while we are updating your profile image..");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingActivity.this,"Profile Image is successfully saved to storage",Toast.LENGTH_LONG).show();
                            SettingUserRef.child("profileImage").setValue(filePath.getPath())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SettingActivity.this, SettingActivity.class);
                                                //selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(selfIntent);
                                                //finish();

                                                Toast.makeText(SettingActivity.this,"Profile Image is successfully saved to database",Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                            else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingActivity.this,"Error occurred" + message,Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }else{
                Toast.makeText(SettingActivity.this,"Error occurred, Image can't be cropped, Try Again..",Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }



    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username)) {
            Toast.makeText(this,"Please write your username", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(profilename)) {
            Toast.makeText(this,"Please write your username", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(status)) {
            Toast.makeText(this,"Please write your status", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(dob)) {
            Toast.makeText(this,"Please write your date of birth", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(country)) {
            Toast.makeText(this,"Please write your country", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(gender)) {
            Toast.makeText(this,"Please write your gender", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(relation)) {
            Toast.makeText(this,"Please write your relation status", Toast.LENGTH_LONG).show();
        }else{
            loadingBar.setTitle("Upate Settings");
            loadingBar.setMessage("Please wait while we are updating your informations");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccountInfo(username,profilename,status,dob,country,gender,relation);
        }
    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation) {
        HashMap userMap = new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",profilename);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",relation);

        SettingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(SettingActivity.this,"Account setting update successfully",Toast.LENGTH_LONG).show();
                    SendUserToMainActivity();
                    loadingBar.dismiss();
                }else{
                    Toast.makeText(SettingActivity.this,"Error occurred while updatting your account information", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent loginIntent = new Intent(SettingActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}
