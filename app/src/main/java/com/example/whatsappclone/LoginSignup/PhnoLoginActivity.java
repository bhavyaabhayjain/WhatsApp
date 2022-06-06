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
import android.widget.Toast;

import com.example.whatsappclone.MainActivity;
import com.example.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhnoLoginActivity extends AppCompatActivity {
    EditText verfication_text,phonenumber;
    Button sendverication_code, Verify;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    String verificationid;
    PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phno_login);
        Intialize();
        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        sendverication_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String PhoneNumber=phonenumber.getText().toString();
                if (TextUtils.isEmpty(PhoneNumber)){
                    phonenumber.setError("Please Enter Your Phone Number");
                }
                else
                {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("Please Wait While we are authentication your phone....");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthOptions options=PhoneAuthOptions.newBuilder(auth).
                            setPhoneNumber("+91"+PhoneNumber)
                            .setTimeout(60l, TimeUnit.SECONDS)
                            .setActivity(PhnoLoginActivity.this)
                            .setCallbacks(callback)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                    auth.setLanguageCode("en");

                }

            }
        });
        callback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                progressDialog.cancel();
                verfication_text.setText(phoneAuthCredential.getSmsCode());
                signinwithphonecredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(), "Invalid Phone Number, Please Enter Correct Phone Number", Toast.LENGTH_SHORT).show();
                Verify.setVisibility(View.INVISIBLE);
                verfication_text.setVisibility(View.INVISIBLE);
                sendverication_code.setVisibility(View.VISIBLE);
                phonenumber.setVisibility(View.VISIBLE);
                if (e instanceof FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(getApplicationContext(), "Invalid Request : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                if (e instanceof FirebaseTooManyRequestsException){
                    Toast.makeText(getApplicationContext(), " Your SMS limit is expried ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken Token) {
                verificationid=s;
                token=Token;
                Toast.makeText(getApplicationContext(), " Code Sent ", Toast.LENGTH_SHORT).show();
                Verify.setVisibility(View.VISIBLE);
                verfication_text.setVisibility(View.VISIBLE);
                sendverication_code.setVisibility(View.INVISIBLE);
                phonenumber.setVisibility(View.INVISIBLE);
            }
        };
        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phonenumber.setVisibility(View.INVISIBLE);
                sendverication_code.setVisibility(View.INVISIBLE);
                String code=verfication_text.getText().toString();
                if (TextUtils.isEmpty(code))
                {
                    verfication_text.setError("Please Enter Verification Code");
                }
                else {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("Please wait, while we are verifying your code");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationid,code);
                    signinwithphonecredentials(credential);
                }
            }
        });
    }

    private void Intialize() {
        verfication_text=findViewById(R.id.phone_number_verify_code);
        sendverication_code=findViewById(R.id.send_verify_code_btn);
        Verify=findViewById(R.id.verify_btn);
        phonenumber=findViewById(R.id.phone_number_edit);
        Verify.setVisibility(View.INVISIBLE);
        verfication_text.setVisibility(View.INVISIBLE);
        sendverication_code.setVisibility(View.VISIBLE);
        phonenumber.setVisibility(View.VISIBLE);

    }
    public void signinwithphonecredentials(PhoneAuthCredential phoneAuthCredential)
    {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "you are succesfully logged in", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }
                else {
                    String message=task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error : " + message, Toast.LENGTH_SHORT).show();

                }
                progressDialog.dismiss();
                progressDialog.cancel();
            }
        });
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(PhnoLoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}