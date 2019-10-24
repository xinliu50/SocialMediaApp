package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUsertoMainActivity();
        }
    }

    private void SendUsertoMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void initialUI() {
        UserEmail = (EditText)findViewById(R.id.register_email);
        UserPassword = (EditText)findViewById(R.id.register_password);
        UserConfirmPassword = (EditText)findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button)findViewById(R.id.register_create_account);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please write your email..", Toast.LENGTH_LONG).show();;
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please write your password..", Toast.LENGTH_LONG).show();;
        }
        else if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this,"Please confirm your password..", Toast.LENGTH_LONG).show();;
        }
        else if(!password.equals(confirmPassword)){
            Toast.makeText(this,"Your passwords do not match..", Toast.LENGTH_LONG).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait while we are creating your Account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendEmailVerificationMessage();
                                loadingBar.dismiss();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error occurred: "+message,Toast.LENGTH_LONG).show();;
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUsertoLoginActivity() {
        Intent logIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logIntent);
        finish();
    }

    private void SendEmailVerificationMessage(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this,"Register Successful, we've send you a email. Please check your email and verify your account...",Toast.LENGTH_LONG).show();
                        SendUsertoLoginActivity();
                        mAuth.signOut();
                    }else{
                        Toast.makeText(RegisterActivity.this,"Error..."+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }
}
