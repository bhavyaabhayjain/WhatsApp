package com.example.whatsappclone.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {
    TextView needanewaccount, forgotpassword;
    Button phonenumberlogin, LogIn;
    EditText email,password;
    DatabaseReference UserRef;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intialize();
        progressDialog= new ProgressDialog(this);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        needanewaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Senduserstoregisteractivity();
            }
        });

    phonenumberlogin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           Senduserstophoneactivity();
        }
    });
    LogIn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AllowUserLogIn();
        }
    });
    forgotpassword.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendlinktomail();
        }
    });
}

    private void sendlinktomail() {
        if (email.getText().toString().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$") && email.getText().toString().length()>8){
            AlertDialog.Builder passwordreset=new AlertDialog.Builder(this);
            passwordreset.setTitle("Reset Password ? ");
            passwordreset.setMessage("Press Yes to receive the rest link");
            passwordreset.setPositiveButton("YES",(dialogInterface, i) -> {
                String resetEmail=email.getText().toString();
                auth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Reset Email Link has been send to your email ID", Toast.LENGTH_SHORT).show();

                    }
                });
            });
            passwordreset.setNegativeButton("No",(dialogInterface, i) -> { });
            passwordreset.create().show();

        }
        else {
            email.setError("Please Enter a Valid Email");
        }
    }

    private void AllowUserLogIn() {
        String userEmail=email.getText().toString();
        String userPass=password.getText().toString();
        if (TextUtils.isEmpty(userEmail)){
            email.setError("Please Enter Email Id");
        }
        if (TextUtils.isEmpty(userPass)){
            password.setError("Please Enter Password");
        }
        if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass)){
            email.setError("Please Enter Email Id");
            password.setError("Please Enter Password");
        }
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {
            progressDialog.setTitle("Signing In");
            progressDialog.setMessage("Please Wait While Logging in Your Account.....");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.signInWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(), "Logged in Successfully", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    else {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+ message, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }

            });
        }
    }

    private void Senduserstoregisteractivity() {

            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
    }
        private void Senduserstophoneactivity() {

            Intent intent = new Intent(LoginActivity.this, PhnoLoginActivity.class);
            startActivity(intent);
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void Intialize()
    {
        needanewaccount=findViewById(R.id.needanewaccount);
        phonenumberlogin=findViewById(R.id.phone_number_login);
        email=findViewById(R.id.login_email);
        password=findViewById(R.id.login_password);
        LogIn=findViewById(R.id.login_btn);
        forgotpassword=findViewById(R.id.forgot_password);
        auth=FirebaseAuth.getInstance();
    }
}