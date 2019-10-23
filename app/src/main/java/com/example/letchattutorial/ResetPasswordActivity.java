package com.example.letchattutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        InitialUI();
        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = ResetEmailInput.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPasswordActivity.this,"Please write your valid email address first...", Toast.LENGTH_LONG).show();;
                }else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this,"Please check your email account...",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }else{
                                Toast.makeText(ResetPasswordActivity.this,"Error Occurred"+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void InitialUI() {
        ResetPasswordSendEmailButton = (Button)findViewById(R.id.reset_password_email_button);
        ResetEmailInput = (EditText)findViewById(R.id.reset_password_EMAIL);
        mToolbar = (Toolbar)findViewById(R.id.forget_password_toobar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        mAuth = FirebaseAuth.getInstance();
    }
}
