package com.example.whatsappclone.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappclone.MainActivity;
import com.example.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {
    EditText regEmail,regPassword;
    TextView alreadyhaveanAccount;
    Button createanewaccount;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference Rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Intialize();
        auth=FirebaseAuth.getInstance();
        Rootref= FirebaseDatabase.getInstance().getReference();
        alreadyhaveanAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });
        createanewaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createanewaccount();
            }
        });
    }
private void createanewaccount()
{
    String userEmail=regEmail.getText().toString();
    String userPass=regPassword.getText().toString();
    if (TextUtils.isEmpty(userEmail)){
        regEmail.setError("Please Enter Email Id");
    }
    if (TextUtils.isEmpty(userPass)){
        regPassword.setError("Please Enter Password");
    }
    if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass)){
        regEmail.setError("Please Enter Email Id");
        regPassword.setError("Please Enter Password");
    }
    if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)){
        progressDialog.setTitle("Create New Account");
        progressDialog.setMessage("Please Wait While Creating Your Account.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        auth.createUserWithEmailAndPassword(userEmail,userPass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            final String[] deviceToken = new String[1];
                            FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    deviceToken[0] =task.getResult();

                                }
                            });
                            String currentUserId=auth.getCurrentUser().getUid();
                            Rootref.child("Users").child(currentUserId).setValue("");
                            Rootref.child("Users").child(currentUserId).child("device_token").setValue(deviceToken[0]);
                            SendUserToMainActivity();
                            Toast.makeText(getApplicationContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error Occured while creating account", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                        progressDialog.dismiss();
                    }
                });
    }
}
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void Intialize() {
        regEmail=findViewById(R.id.signup_email);
        regPassword=findViewById(R.id.signup_password);
        alreadyhaveanAccount=findViewById(R.id.already_have_an_account);
        createanewaccount=findViewById(R.id.signup_btn);
        progressDialog= new ProgressDialog(this);

    }
}